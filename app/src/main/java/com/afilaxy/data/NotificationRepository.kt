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
            val data = mapOf(
                "type" to "emergency_request",
                "title" to "Emergência de Asma",
                "body" to "$requesterName precisa de ajuda a ${distance}m de você",
                "requesterName" to requesterName,
                "distance" to distance
            )

            // In a real app, you would call your backend API here
            // For now, we'll just save the notification request to Firestore
            firestore.collection("notifications")
                .add(data)
                .await()
        } catch (e: Exception) {
            // Log error silently
        }
    }
}