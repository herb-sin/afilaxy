package com.afilaxy.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

import javax.inject.Inject
import com.afilaxy.security.SecureLogger

class NotificationRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val messaging: FirebaseMessaging
) {

    suspend fun saveUserToken(userId: String) {
        if (userId.isBlank()) {
            SecureLogger.w("NotificationRepository", "ID de usuário inválido")
            return
        }
        
        try {
            val token = messaging.token.await()
            firestore.collection("users")
                .document(userId)
                .set(mapOf("fcmToken" to token), com.google.firebase.firestore.SetOptions.merge())
                .await()
        } catch (e: Exception) {
            SecureLogger.e("NotificationRepository", "Erro ao salvar token: ${e.javaClass.simpleName}", null)
        }
    }

    suspend fun sendEmergencyNotification(helperTokens: List<String>, requesterName: String, distance: String) {
        if (helperTokens.isEmpty()) {
            SecureLogger.w("NotificationRepo", "Nenhum token de helper fornecido")
            return
        }
        
        try {
            SecureLogger.d("NotificationRepo", "Enviando FCM para ${helperTokens.size} helpers")
            
            for (token in helperTokens) {
                if (token.isBlank()) continue
                
                SecureLogger.d("NotificationRepo", "Preparando notificação para Firebase Function")
                
                // Salvar no Firestore - Firebase Function vai processar
                val data = mapOf(
                    "type" to "emergency_request",
                    "title" to "🆘 Emergência de Asma",
                    "body" to "Alguém precisa de ajuda a ${distance}m de você",
                    "distance" to distance,
                    "fcmToken" to token,
                    "requesterName" to requesterName,
                    "timestamp" to System.currentTimeMillis(),
                    "sent" to false
                )
                
                firestore.collection("notifications")
                    .add(data)
                    .await()
                    
                SecureLogger.d("NotificationRepo", "✅ FCM enviado para token")
            }
        } catch (e: Exception) {
            SecureLogger.e("NotificationRepo", "Erro ao enviar FCM: ${e.javaClass.simpleName}", null)
        }
    }
}