package com.afilaxy.data.repository

import com.afilaxy.domain.model.Emergency
import com.afilaxy.domain.model.Helper
import com.afilaxy.domain.model.Location
import com.afilaxy.domain.repository.EmergencyRepository
import com.afilaxy.performance.LogOptimizer
import com.afilaxy.security.AuthGuard
import com.afilaxy.security.InputSanitizer
import com.afilaxy.security.SecureLogger
import com.afilaxy.security.SecurityUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmergencyRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : EmergencyRepository {

    override suspend fun createEmergency(emergency: Emergency): Result<String> {
        // Implementation for the object-based create (keeping existing logic structure but adapting to "emergency_requests" if needed, 
        // but for now I'll prioritize the lat/lon one which is used by the app)
        return createEmergency(emergency.location.latitude, emergency.location.longitude)
    }

    override suspend fun createEmergency(latitude: Double, longitude: Double): Result<String> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(SecurityException("User not authenticated"))
            
            // Buscar nome do usuário no Firestore
            val userDoc = firestore.collection("users").document(userId).get().await()
            val userName = userDoc.getString("name") ?: auth.currentUser?.displayName ?: "Usuário"
            
            LogOptimizer.d("EmergencyRepository", "Criando emergência para $userName em $latitude, $longitude")
            
            val emergencyData = mapOf(
                "requesterId" to userId,
                "requesterName" to userName,
                "location" to GeoPoint(latitude, longitude),
                "latitude" to latitude,
                "longitude" to longitude,
                "status" to "waiting",
                "active" to true,
                "timestamp" to System.currentTimeMillis(),
                "expiresAt" to (System.currentTimeMillis() + 600000) // 10 min
            )
            
            val docRef = firestore.collection("emergency_requests")
                .add(emergencyData)
                .await()
            
            LogOptimizer.d("EmergencyRepository", "Emergência criada com ID: ${docRef.id}")
            Result.success(docRef.id)
        } catch (e: Exception) {
            LogOptimizer.e("EmergencyRepository", "Erro ao criar emergência", e)
            Result.failure(e)
        }
    }

    override suspend fun cancelEmergency(emergencyId: String): Result<Boolean> {
        return try {
            firestore.collection("emergency_requests")
                .document(emergencyId)
                .update("active", false, "status", "cancelled")
                .await()
            Result.success(true)
        } catch (e: Exception) {
            LogOptimizer.e("EmergencyRepository", "Erro ao cancelar emergência", e)
            Result.failure(e)
        }
    }

    override suspend fun activateHelper(latitude: Double, longitude: Double): Result<Boolean> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(SecurityException("User not authenticated"))
            val userEmail = auth.currentUser?.email ?: ""
            
            LogOptimizer.d("EmergencyRepository", "Ativando helper $userId em $latitude, $longitude")
            
            val helperData = mapOf(
                "id" to userId,
                "email" to userEmail,
                "location" to GeoPoint(latitude, longitude),
                "isActive" to true,
                "lastUpdate" to System.currentTimeMillis()
            )
            
            firestore.collection("helpers")
                .document(userId)
                .set(helperData)
                .await()
            
            LogOptimizer.d("EmergencyRepository", "Helper ativado com sucesso")
            Result.success(true)
        } catch (e: Exception) {
            LogOptimizer.e("EmergencyRepository", "Erro ao ativar helper", e)
            Result.failure(e)
        }
    }

    override suspend fun deactivateHelper(): Result<Boolean> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(SecurityException("User not authenticated"))
            LogOptimizer.d("EmergencyRepository", "Desativando helper $userId")
            firestore.collection("helpers")
                .document(userId)
                .delete()
                .await()
            LogOptimizer.d("EmergencyRepository", "Helper desativado com sucesso")
            Result.success(true)
        } catch (e: Exception) {
            LogOptimizer.e("EmergencyRepository", "Erro ao desativar helper", e)
            Result.failure(e)
        }
    }

    override suspend fun acceptEmergency(emergencyId: String): Result<Boolean> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(SecurityException("User not authenticated"))
            val userDoc = firestore.collection("users").document(userId).get().await()
            val helperName = userDoc.getString("name") ?: auth.currentUser?.displayName ?: "Helper"
            
            LogOptimizer.d("EmergencyRepository", "Helper $helperName tentando aceitar emergência $emergencyId")
            
            // Usar transação atômica para evitar que 2 helpers aceitem a mesma emergência
            firestore.runTransaction { transaction ->
                val emergencyRef = firestore.collection("emergency_requests").document(emergencyId)
                val emergencyDoc = transaction.get(emergencyRef)
                
                if (!emergencyDoc.exists()) {
                    throw Exception("Emergência não encontrada")
                }
                
                val isActive = emergencyDoc.getBoolean("active") ?: false
                val currentHelperId = emergencyDoc.getString("helperId")
                val currentStatus = emergencyDoc.getString("status") ?: ""
                
                // Verificar se já tem helper ou status não é "waiting"
                if (!isActive) {
                    throw Exception("Emergência não está ativa")
                }
                
                if (currentHelperId != null || currentStatus != "waiting") {
                    throw Exception("Emergência já foi aceita por outro helper")
                }
                
                // Calcular novo expiresAt: 10 minutos a partir do match
                val newExpiresAt = System.currentTimeMillis() + 600000 // 10 min
                
                // Atualizar atomicamente - só acontece se ninguém mais atualizou antes
                transaction.update(
                    emergencyRef,
                    mapOf(
                        "status" to "matched",
                        "helperId" to userId,
                        "helperName" to helperName,
                        "matchedAt" to System.currentTimeMillis(),
                        "expiresAt" to newExpiresAt
                    )
                )
                
                LogOptimizer.d("EmergencyRepository", "Emergência aceita com sucesso - Helper: $helperName")
            }.await()
            
            Result.success(true)
        } catch (e: Exception) {
            LogOptimizer.e("EmergencyRepository", "Erro ao aceitar emergência: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun getActiveEmergency(): Result<String?> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(SecurityException("User not authenticated"))
            val currentTime = System.currentTimeMillis()
            
            // Buscar emergências do usuário como requester
            val requesterQuery = firestore.collection("emergency_requests")
                .whereEqualTo("requesterId", userId)
                .whereEqualTo("active", true)
                .get()
                .await()
            
            for (doc in requesterQuery.documents) {
                val expiresAt = doc.getLong("expiresAt") ?: 0
                if (expiresAt > currentTime) {
                    return Result.success(doc.id)
                }
            }
            
            // Buscar emergências do usuário como helper
            val helperQuery = firestore.collection("emergency_requests")
                .whereEqualTo("helperId", userId)
                .whereEqualTo("active", true)
                .get()
                .await()
            
            for (doc in helperQuery.documents) {
                val expiresAt = doc.getLong("expiresAt") ?: 0
                if (expiresAt > currentTime) {
                    return Result.success(doc.id)
                }
            }
            
            Result.success(null)
        } catch (e: Exception) {
            LogOptimizer.e("EmergencyRepository", "Erro ao verificar emergência ativa", e)
            Result.failure(e)
        }
    }

    override suspend fun clearUserEmergencies(): Result<Boolean> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(SecurityException("User not authenticated"))
            LogOptimizer.d("EmergencyRepository", "Limpando emergências do usuário $userId")
            
            // Cancelar emergências como requester
            val requesterQuery = firestore.collection("emergency_requests")
                .whereEqualTo("requesterId", userId)
                .whereEqualTo("active", true)
                .get()
                .await()
            
            for (doc in requesterQuery.documents) {
                doc.reference.update(
                    "active", false,
                    "status", "cancelled",
                    "cancelledAt", System.currentTimeMillis()
                ).await()
            }
            
            // Cancelar emergências como helper
            val helperQuery = firestore.collection("emergency_requests")
                .whereEqualTo("helperId", userId)
                .whereEqualTo("active", true)
                .get()
                .await()
            
            for (doc in helperQuery.documents) {
                doc.reference.update(
                    "active", false,
                    "status", "cancelled",
                    "cancelledAt", System.currentTimeMillis()
                ).await()
            }
            
            // Desativar helper
            deactivateHelper()
            
            LogOptimizer.d("EmergencyRepository", "Emergências limpas com sucesso")
            Result.success(true)
        } catch (e: Exception) {
            LogOptimizer.e("EmergencyRepository", "Erro ao limpar emergências", e)
            Result.failure(e)
        }
    }

    override suspend fun isHelperActive(): Result<Boolean> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(SecurityException("User not authenticated"))
            val helperDoc = firestore.collection("helpers").document(userId).get().await()
            val isActive = helperDoc.exists() && helperDoc.getBoolean("isActive") == true
            Result.success(isActive)
        } catch (e: Exception) {
            LogOptimizer.e("EmergencyRepository", "Erro ao verificar status do helper", e)
            Result.failure(e)
        }
    }

    override suspend fun findNearbyHelpers(location: Location, radiusKm: Double): Result<List<Helper>> {
        // Placeholder implementation to satisfy interface - logic not fully ported from original Impl yet as it wasn't in EmergencyManager
        return Result.success(emptyList())
    }

    override suspend fun updateEmergencyStatus(emergencyId: String, status: String): Result<Unit> {
         return try {
            firestore.collection("emergency_requests")
                .document(emergencyId)
                .update("status", status)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun finishEmergency(emergencyId: String): Result<Boolean> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(SecurityException("User not authenticated"))
            
            LogOptimizer.d("EmergencyRepository", "Finalizando emergência $emergencyId por usuário $userId")
            
            // Verificar se emergência existe
            val emergencyDoc = firestore.collection("emergency_requests").document(emergencyId).get().await()
            if (!emergencyDoc.exists()) {
                LogOptimizer.e("EmergencyRepository", "Emergência $emergencyId não existe")
                return Result.failure(Exception("Emergência não encontrada"))
            }
            
            // Atualizar status da emergência
            firestore.collection("emergency_requests")
                .document(emergencyId)
                .update(
                    "status", "resolved",
                    "active", false,
                    "resolvedAt", System.currentTimeMillis(),
                    "resolvedBy", userId
                )
                .await()
            
            LogOptimizer.d("EmergencyRepository", "Emergência finalizada com sucesso - Status: resolved, active: false")
            Result.success(true)
        } catch (e: Exception) {
            LogOptimizer.e("EmergencyRepository", "Erro ao finalizar emergência", e)
            Result.failure(e)
        }
    }
}