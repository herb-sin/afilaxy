package com.afilaxy.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

class NotificationRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val messaging = FirebaseMessaging.getInstance()

    suspend fun saveUserToken(userId: String) {
        try {
            val token = messaging.token.await()
            firestore.collection("users")
                .document(userId)
                .update("fcmToken", token)
                .await()
        } catch (e: Exception) {
            // Log error silently
        }
    }

    suspend fun sendEmergencyNotification(helperTokens: List<String>, requesterName: String, distance: String) {
        try {
            android.util.Log.d("NotificationRepo", "🚨 ENVIANDO FCM para ${helperTokens.size} helpers")
            
            for (token in helperTokens) {
                android.util.Log.d("NotificationRepo", "📱 Enviando para token: ${token.take(20)}...")
                
                val data = mapOf(
                    "type" to "emergency_request",
                    "title" to "🆘 Emergência de Asma",
                    "body" to "$requesterName precisa de ajuda a ${distance}m de você",
                    "requesterName" to requesterName,
                    "distance" to distance,
                    "fcmToken" to token,
                    "timestamp" to System.currentTimeMillis()
                )

                // Salvar notificação no Firestore para debug
                firestore.collection("notifications")
                    .add(data)
                    .await()
                    
                android.util.Log.d("NotificationRepo", "✅ Notificação salva no Firestore")
            }
        } catch (e: Exception) {
            android.util.Log.e("NotificationRepo", "❌ Erro ao enviar FCM: ${e.message}")
        }
    }
}