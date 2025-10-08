package com.afilaxy.notification

import androidx.navigation.NavController
import com.afilaxy.security.AuthGuard
import com.afilaxy.security.InputSanitizer
import com.afilaxy.utils.ErrorHandler
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class NotificationManager {
    
    private var notificationListener: ListenerRegistration? = null
    private val firestore = FirebaseFirestore.getInstance()
    
    fun setupNotificationListener(navController: NavController) {
        if (!AuthGuard.isUserAuthenticated()) {
            com.afilaxy.security.SecurityUtils.safeLog("NotificationManager", "Setup denied - authentication required", com.afilaxy.security.SecurityUtils.LogLevel.WARN)
            return
        }
        
        val currentUser = AuthGuard.getCurrentUser() ?: return
        
        notificationListener = firestore
            .collection("users")
            .document(currentUser.uid)
            .collection("notifications")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    val errorResult = ErrorHandler.handleError(error, "notificationListener")
                    android.util.Log.e("NotificationManager", errorResult.logMessage)
                    return@addSnapshotListener
                }
                
                snapshot?.documentChanges?.forEach { change ->
                    if (change.type == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                        processNotification(change.document, navController, currentUser.uid)
                    }
                }
            }
    }
    
    private fun processNotification(
        doc: com.google.firebase.firestore.DocumentSnapshot,
        navController: NavController,
        currentUserId: String
    ) {
        val type = doc.getString("type")
        val processed = doc.getBoolean("processed") ?: false
        
        if (type == "emergency_alert" && !processed) {
            val emergencyId = com.afilaxy.security.SecureValidator.validateAndSanitizeInput(doc.getString("emergencyId"), 50)
            val requesterId = com.afilaxy.security.SecureValidator.validateAndSanitizeInput(doc.getString("requesterId"), 128)
            
            // Mark as processed immediately to prevent reprocessing
            try {
                doc.reference.update("processed", true)
            } catch (e: Exception) {
                com.afilaxy.security.SecurityUtils.safeLog("NotificationManager", "Failed to mark notification as processed", com.afilaxy.security.SecurityUtils.LogLevel.ERROR)
            }
            
            // Navigate only if valid and not self-request
            if (requesterId.isNotBlank() && requesterId != currentUserId && emergencyId.isNotBlank()) {
                try {
                    navController.navigate("tela_helper_response/$emergencyId")
                } catch (e: Exception) {
                    com.afilaxy.security.SecurityUtils.safeLog("NotificationManager", "Navigation failed", com.afilaxy.security.SecurityUtils.LogLevel.ERROR)
                }
            }
        }
    }
    
    fun cleanup() {
        notificationListener?.remove()
        notificationListener = null
    }
}