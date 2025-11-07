package com.afilaxy.data.repository

import com.afilaxy.domain.model.EmergencyRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class EmergencyRequestRepository {
    
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    suspend fun createEmergencyRequest(latitude: Double, longitude: Double): String? {
        return try {
            val user = auth.currentUser ?: return null
            
            val request = EmergencyRequest(
                id = firestore.collection("emergency_requests").document().id,
                requesterId = user.uid,
                requesterName = user.email?.substringBefore("@") ?: "Usuário",
                latitude = latitude,
                longitude = longitude
            )
            
            android.util.Log.d("EmergencyRequestRepository", "Criando pedido de emergência: ${request.id}")
            
            firestore.collection("emergency_requests")
                .document(request.id)
                .set(request)
                .await()
            
            android.util.Log.d("EmergencyRequestRepository", "Pedido criado com sucesso, expira em 5 minutos")
            request.id
        } catch (e: Exception) {
            android.util.Log.e("EmergencyRequestRepository", "Erro ao criar pedido: ${e.message}")
            null
        }
    }
    
    suspend fun cancelEmergencyRequest(requestId: String): Boolean {
        return try {
            android.util.Log.d("EmergencyRequestRepository", "Cancelando pedido: $requestId")
            
            firestore.collection("emergency_requests")
                .document(requestId)
                .update("isActive", false)
                .await()
            
            android.util.Log.d("EmergencyRequestRepository", "Pedido cancelado com sucesso")
            true
        } catch (e: Exception) {
            android.util.Log.e("EmergencyRequestRepository", "Erro ao cancelar pedido: ${e.message}")
            false
        }
    }
    
    suspend fun cleanupExpiredRequests() {
        try {
            val currentTime = System.currentTimeMillis()
            
            val expiredRequests = firestore.collection("emergency_requests")
                .whereEqualTo("isActive", true)
                .whereLessThan("expiresAt", currentTime)
                .get()
                .await()
            
            android.util.Log.d("EmergencyRequestRepository", "Limpando ${expiredRequests.size()} pedidos expirados")
            
            for (doc in expiredRequests.documents) {
                doc.reference.update("isActive", false).await()
            }
        } catch (e: Exception) {
            android.util.Log.e("EmergencyRequestRepository", "Erro na limpeza: ${e.message}")
        }
    }
}