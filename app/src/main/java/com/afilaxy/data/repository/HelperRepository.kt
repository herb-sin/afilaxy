package com.afilaxy.data.repository

import com.afilaxy.domain.model.Helper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.tasks.await
import kotlin.math.*

class HelperRepository {
    
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    /**
     * Salva/atualiza o usuário como helper ativo
     */
    suspend fun saveHelper(latitude: Double, longitude: Double): Boolean {
        return try {
            val user = auth.currentUser
            if (user == null) {
                android.util.Log.w("HelperRepository", "Usuário não autenticado")
                return false
            }
            
            android.util.Log.d("HelperRepository", "Salvando helper em localização")
            
            val helper = mapOf(
                "id" to user.uid,
                "name" to (user.displayName ?: "Helper"),
                "email" to (user.email ?: ""),
                "location" to GeoPoint(latitude, longitude),
                "isActive" to true,
                "lastUpdate" to System.currentTimeMillis()
            )
            
            firestore.collection("helpers")
                .document(user.uid)
                .set(helper)
                .await()
            
            android.util.Log.d("HelperRepository", "Helper salvo com sucesso")
            true
        } catch (e: Exception) {
            android.util.Log.e("HelperRepository", "Erro ao salvar helper", e)
            false
        }
    }
    
    /**
     * Remove o usuário como helper ativo
     */
    suspend fun removeHelper(): Boolean {
        return try {
            val user = auth.currentUser
            if (user == null) {
                android.util.Log.w("HelperRepository", "Usuário não autenticado para remover helper")
                return false
            }
            
            android.util.Log.d("HelperRepository", "Removendo helper ativo")
            
            firestore.collection("helpers")
                .document(user.uid)
                .delete()
                .await()
            
            android.util.Log.d("HelperRepository", "Helper removido com sucesso")
            true
        } catch (e: Exception) {
            android.util.Log.e("HelperRepository", "Erro ao remover helper", e)
            false
        }
    }
    
    /**
     * Busca helpers próximos (raio padrão de 260 metros)
     */
    suspend fun getNearbyHelpers(latitude: Double, longitude: Double, radiusKm: Double = 5.0): List<Helper> {
        return try {
            val currentUserId = auth.currentUser?.uid
            android.util.Log.d("HelperRepository", "Buscando helpers próximos")
            
            val snapshot = firestore.collection("helpers")
                .get()
                .await()
            
            android.util.Log.d("HelperRepository", "Processando ${snapshot.documents.size} helpers")
            
            val helpers = snapshot.documents
                .mapNotNull { document ->
                    val data = document.data ?: return@mapNotNull null
                    val helperId = data["id"] as? String ?: return@mapNotNull null
                    
                    if (helperId == currentUserId) return@mapNotNull null
                    
                    val location = data["location"] as? GeoPoint ?: return@mapNotNull null
                    val distance = calculateDistance(latitude, longitude, location.latitude, location.longitude)
                    
                    if (distance <= radiusKm) {
                        Helper(
                            id = helperId,
                            name = "Helper Próximo",
                            email = "",
                            latitude = location.latitude,
                            longitude = location.longitude,
                            isActive = data["isActive"] as? Boolean ?: false,
                            lastUpdate = data["lastUpdate"] as? Long ?: 0L,
                            distance = distance
                        )
                    } else null
                }
                .sortedBy { it.distance }
            
            android.util.Log.d("HelperRepository", "Encontrados ${helpers.size} helpers próximos")
            helpers
        } catch (e: Exception) {
            android.util.Log.e("HelperRepository", "Erro ao buscar helpers próximos", e)
            emptyList()
        }
    }
    
    /**
     * Calcula distância entre duas coordenadas (Haversine)
     */
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371.0 // Raio da Terra em km
        
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        
        val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        
        return R * c
    }
}