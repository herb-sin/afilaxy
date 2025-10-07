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
        val currentUser = AuthGuard.getCurrentUser()
        if (currentUser == null) {
            android.util.Log.w("NotificationManager", "Usuário não autenticado")
            return
        }
        
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
            val emergencyId = InputSanitizer.sanitizeText(doc.getString("emergencyId"))
            val requesterId = InputSanitizer.sanitizeText(doc.getString("requesterId"))
            
            // Marcar como processado
            ErrorHandler.safeCall("markNotificationProcessed") {
                doc.reference.update("processed", true)
            }
            
            // Verificar se não é o próprio usuário
            if (!requesterId.isNullOrBlank() && requesterId != currentUserId) {
                if (!emergencyId.isNullOrBlank()) {
                    navController.navigate("tela_helper_response/$emergencyId")
                } else {
                    navController.navigate("tela_helper_response")
                }
            }
        }
    }
    
    fun cleanup() {
        notificationListener?.remove()
        notificationListener = null
    }
}