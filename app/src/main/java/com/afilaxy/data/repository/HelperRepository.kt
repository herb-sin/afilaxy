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
            
            android.util.Log.d("HelperRepository", "Salvando helper: ${user.uid} em ($latitude, $longitude)")
            
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
            android.util.Log.e("HelperRepository", "Erro ao salvar helper: ${e.message}")
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
            
            android.util.Log.d("HelperRepository", "Removendo usuário ${user.uid} como helper ativo")
            
            firestore.collection("helpers")
                .document(user.uid)
                .update("isActive", false)
                .await()
            
            android.util.Log.d("HelperRepository", "Helper removido com sucesso")
            true
        } catch (e: Exception) {
            android.util.Log.e("HelperRepository", "Erro ao remover helper: ${e.message}")
            false
        }
    }
    
    /**
     * Busca helpers próximos (raio padrão de 260 metros)
     */
    suspend fun getNearbyHelpers(latitude: Double, longitude: Double, radiusKm: Double = 0.26): List<Helper> {
        return try {
            val currentUserId = auth.currentUser?.uid
            android.util.Log.d("HelperRepository", "Buscando helpers próximos em ($latitude, $longitude) com raio de ${radiusKm}km (excluindo usuário atual: $currentUserId)")
            
            val snapshot = firestore.collection("helpers")
                .whereEqualTo("isActive", true)
                .get()
                .await()
            
            android.util.Log.d("HelperRepository", "Encontrados ${snapshot.documents.size} helpers ativos no banco")
            
            val helpers = mutableListOf<Helper>()
            
            for (document in snapshot.documents) {
                val data = document.data ?: continue
                val helperId = data["id"] as? String ?: ""
                
                // Excluir o próprio usuário
                if (helperId == currentUserId) {
                    android.util.Log.d("HelperRepository", "Excluindo o próprio usuário da lista de helpers")
                    continue
                }
                
                val location = data["location"] as? GeoPoint ?: continue
                
                val distance = calculateDistance(
                    latitude, longitude,
                    location.latitude, location.longitude
                )
                
                android.util.Log.d("HelperRepository", "Helper ${data["name"]} está a ${String.format("%.2f", distance)}km de distância")
                
                if (distance <= radiusKm) {
                    val helper = Helper(
                        id = helperId,
                        name = "Helper Próximo", // Anonimizado por segurança
                        email = "", // Não expor email
                        latitude = location.latitude,
                        longitude = location.longitude,
                        isActive = data["isActive"] as? Boolean ?: false,
                        lastUpdate = data["lastUpdate"] as? Long ?: 0L,
                        distance = distance
                    )
                    helpers.add(helper)
                    android.util.Log.d("HelperRepository", "Helper anônimo adicionado à lista (distância: ${String.format("%.0f", distance * 1000)}m)")
                }
            }
            
            android.util.Log.d("HelperRepository", "Total de helpers próximos encontrados: ${helpers.size}")
            
            // Ordena por distância
            helpers.sortedBy { it.distance }
        } catch (e: Exception) {
            android.util.Log.e("HelperRepository", "Erro ao buscar helpers próximos: ${e.message}")
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