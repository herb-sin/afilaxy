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
            Log.d(TAG, "🔄 Iniciando atualização do token FCM...")
            
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser == null) {
                Log.w(TAG, "❌ Usuário não autenticado")
                return
            }
            
            Log.d(TAG, "👤 Usuário autenticado: ${currentUser.uid}")
            
            val token = FirebaseMessaging.getInstance().token.await()
            Log.d(TAG, "🔑 Token FCM obtido: ${token.take(20)}...")
            
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUser.uid)
                .update(mapOf(
                    "fcmToken" to token,
                    "tokenUpdatedAt" to System.currentTimeMillis()
                ))
                .await()
            
            Log.d(TAG, "✅ Token FCM atualizado com sucesso no Firestore")
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao atualizar token FCM: ${e.message}")
            Log.e(TAG, "❌ Stack trace: ${e.stackTrace.joinToString("\n")}")
        }
    }
}