package com.afilaxy.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.tasks.await

/**
 * Gerenciador central de emergências - Backend simplificado
 */
object EmergencyManager {
    
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    /**
     * Cria pedido de emergência
     * Retorna: emergencyId ou null se falhou
     */
    suspend fun createEmergency(latitude: Double, longitude: Double): String? {
        return try {
            val userId = auth.currentUser?.uid ?: return null
            
            // Buscar nome do usuário no Firestore
            val userDoc = firestore.collection("users").document(userId).get().await()
            val userName = userDoc.getString("name") ?: auth.currentUser?.displayName ?: "Usuário"
            
            android.util.Log.d("EmergencyManager", "Criando emergência para $userName em $latitude, $longitude")
            
            val emergencyData = mapOf(
                "requesterId" to userId,
                "requesterName" to userName,
                "location" to GeoPoint(latitude, longitude),
                "latitude" to latitude,
                "longitude" to longitude,
                "status" to "waiting",
                "active" to true,
                "timestamp" to System.currentTimeMillis(),
                "expiresAt" to (System.currentTimeMillis() + 300000) // 5 min
            )
            
            val docRef = firestore.collection("emergency_requests")
                .add(emergencyData)
                .await()
            
            android.util.Log.d("EmergencyManager", "Emergência criada com ID: ${docRef.id}")
            docRef.id
        } catch (e: Exception) {
            android.util.Log.e("EmergencyManager", "Erro ao criar emergência", e)
            null
        }
    }
    
    /**
     * Cancela emergência
     */
    suspend fun cancelEmergency(emergencyId: String): Boolean {
        return try {
            firestore.collection("emergency_requests")
                .document(emergencyId)
                .update("active", false, "status", "cancelled")
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Ativa usuário como helper
     */
    suspend fun activateHelper(latitude: Double, longitude: Double): Boolean {
        return try {
            val userId = auth.currentUser?.uid ?: return false
            val userEmail = auth.currentUser?.email ?: ""
            
            android.util.Log.d("EmergencyManager", "Ativando helper $userId em $latitude, $longitude")
            
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
            
            android.util.Log.d("EmergencyManager", "Helper ativado com sucesso")
            true
        } catch (e: Exception) {
            android.util.Log.e("EmergencyManager", "Erro ao ativar helper", e)
            false
        }
    }
    
    /**
     * Aceita emergência como helper
     */
    suspend fun acceptEmergency(emergencyId: String): Boolean {
        return try {
            val userId = auth.currentUser?.uid ?: return false
            val userDoc = firestore.collection("users").document(userId).get().await()
            val helperName = userDoc.getString("name") ?: auth.currentUser?.displayName ?: "Helper"
            
            android.util.Log.d("EmergencyManager", "Helper $helperName aceitando emergência $emergencyId")
            
            // Verificar se emergência ainda existe e está ativa
            val emergencyDoc = firestore.collection("emergency_requests").document(emergencyId).get().await()
            if (!emergencyDoc.exists()) {
                android.util.Log.e("EmergencyManager", "Emergência $emergencyId não existe")
                return false
            }
            
            val isActive = emergencyDoc.getBoolean("active") ?: false
            if (!isActive) {
                android.util.Log.e("EmergencyManager", "Emergência $emergencyId não está ativa")
                return false
            }
            
            // Atualizar status da emergência
            firestore.collection("emergency_requests")
                .document(emergencyId)
                .update(
                    "status", "matched",
                    "helperId", userId,
                    "helperName", helperName,
                    "matchedAt", System.currentTimeMillis()
                )
                .await()
            
            android.util.Log.d("EmergencyManager", "Emergência aceita com sucesso - Status: matched")
            true
        } catch (e: Exception) {
            android.util.Log.e("EmergencyManager", "Erro ao aceitar emergência", e)
            false
        }
    }
    
    /**
     * Verifica se usuário tem emergência ativa
     */
    suspend fun getActiveEmergency(): String? {
        return try {
            val userId = auth.currentUser?.uid ?: return null
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
                    return doc.id
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
                    return doc.id
                }
            }
            
            null
        } catch (e: Exception) {
            android.util.Log.e("EmergencyManager", "Erro ao verificar emergência ativa", e)
            null
        }
    }
    
    /**
     * Desativa helper
     */
    suspend fun deactivateHelper(): Boolean {
        return try {
            val userId = auth.currentUser?.uid ?: return false
            android.util.Log.d("EmergencyManager", "Desativando helper $userId")
            firestore.collection("helpers")
                .document(userId)
                .delete()
                .await()
            android.util.Log.d("EmergencyManager", "Helper desativado com sucesso")
            true
        } catch (e: Exception) {
            android.util.Log.e("EmergencyManager", "Erro ao desativar helper", e)
            false
        }
    }
}