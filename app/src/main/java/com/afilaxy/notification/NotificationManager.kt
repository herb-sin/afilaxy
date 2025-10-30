package com.afilaxy.notification

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

class NotificationManager(private val context: Context) {
    
    private val firestore = FirebaseFirestore.getInstance()
    private val messaging = FirebaseMessaging.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun initializeNotifications(): Boolean {
        return try {
            val token = messaging.token.await()
            saveTokenToUser(token)
            true
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun saveTokenToUser(token: String) {
        val userId = auth.currentUser?.uid ?: return
        
        try {
            firestore.collection("users")
                .document(userId)
                .update("fcmToken", token)
                .await()
        } catch (e: Exception) {
            // Create user document if it doesn't exist
            val userData = mapOf(
                "fcmToken" to token,
                "isHelper" to false,
                "createdAt" to System.currentTimeMillis()
            )
            
            firestore.collection("users")
                .document(userId)
                .set(userData)
                .await()
        }
    }

    suspend fun sendEmergencyNotification(
        latitude: Double,
        longitude: Double,
        message: String = "Preciso de ajuda com asma!"
    ): Boolean {
        return try {
            val emergencyData = mapOf(
                "type" to "emergency_request",
                "latitude" to latitude,
                "longitude" to longitude,
                "message" to message,
                "requesterId" to (auth.currentUser?.uid ?: ""),
                "timestamp" to System.currentTimeMillis()
            )

            // Save emergency to Firestore
            val emergencyRef = firestore.collection("emergencies").document()
            emergencyRef.set(emergencyData).await()

            // Find nearby helpers and send notifications
            sendToNearbyHelpers(latitude, longitude, message, emergencyRef.id)
            
            true
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun sendToNearbyHelpers(
        latitude: Double,
        longitude: Double,
        message: String,
        emergencyId: String
    ) {
        try {
            // Query helpers within 5km radius (simplified)
            val helpers = firestore.collection("users")
                .whereEqualTo("isHelper", true)
                .limit(10)
                .get()
                .await()

            helpers.documents.forEach { helper ->
                val token = helper.getString("fcmToken")
                if (!token.isNullOrEmpty()) {
                    // In a real implementation, you would send via Firebase Functions
                    // For now, we'll simulate the notification
                    simulateNotificationSent(token, message)
                }
            }
        } catch (e: Exception) {
            // Handle error
        }
    }

    private fun simulateNotificationSent(token: String, message: String) {
        // This would normally be handled by Firebase Functions
        // For development, we'll just log it
        println("Notification sent to token: ${token.take(10)}... - Message: $message")
    }

    suspend fun respondToEmergency(emergencyId: String, response: String): Boolean {
        return try {
            val responseData = mapOf(
                "emergencyId" to emergencyId,
                "helperId" to (auth.currentUser?.uid ?: ""),
                "response" to response,
                "timestamp" to System.currentTimeMillis()
            )

            firestore.collection("emergency_responses")
                .add(responseData)
                .await()

            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun toggleHelperStatus(isHelper: Boolean): Boolean {
        return try {
            val userId = auth.currentUser?.uid ?: return false
            
            firestore.collection("users")
                .document(userId)
                .update("isHelper", isHelper)
                .await()
            
            true
        } catch (e: Exception) {
            false
        }
    }
}