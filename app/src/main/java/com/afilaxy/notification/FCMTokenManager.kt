package com.afilaxy.notification

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

object FCMTokenManager {
    
    private const val TAG = "FCMTokenManager"
    
    suspend fun updateFCMToken() {
        try {
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser == null) {
                Log.w(TAG, "Usuário não autenticado")
                return
            }
            
            val token = FirebaseMessaging.getInstance().token.await()
            
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUser.uid)
                .update(mapOf(
                    "fcmToken" to token,
                    "tokenUpdatedAt" to System.currentTimeMillis()
                ))
                .await()
            
            Log.d(TAG, "Token FCM atualizado com sucesso")
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao atualizar token FCM: ${e.message}")
        }
    }
}