package com.afilaxy

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import android.content.Context

fun saveFcmTokenToFirestore(context: Context) {
    // Executar em background thread para evitar ANR
    Thread {
        try {
            val user = FirebaseAuth.getInstance().currentUser
            
            // Verificação crítica de autenticação
            if (user == null) {
                android.util.Log.e("FirebaseUtils", "Tentativa de salvar token FCM sem autenticação")
                return@Thread
            }
            
            if (!user.isEmailVerified) {
                android.util.Log.w("FirebaseUtils", "Salvando token FCM com email não verificado")
                // Continua mesmo sem verificação para funcionalidade de emergência
            }
            
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    val db = FirebaseFirestore.getInstance()
                    val userData = mapOf(
                        "fcmToken" to token,
                        "createdAt" to System.currentTimeMillis(),
                        "isHelper" to false
                    )
                    db.collection("users").document(user.uid)
                        .set(userData)
                        .addOnSuccessListener {
                            android.util.Log.d("FirebaseUtils", "Token FCM salvo com sucesso")
                        }
                        .addOnFailureListener { e ->
                            android.util.Log.e("FirebaseUtils", "Erro ao salvar token FCM: ${e.message}")
                        }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("FirebaseUtils", "Erro ao processar token FCM: ${e.message}")
        }
    }.start()
}

fun sendAfilaxyAlert(
    tokens: List<String>,
    nomePaciente: String,
    latitude: Double,
    longitude: Double
) {
    val data = hashMapOf(
        "tokens" to tokens,
        "nomePaciente" to nomePaciente,
        "latitude" to latitude,
        "longitude" to longitude
    )
    FirebaseFunctions.getInstance()
        .getHttpsCallable("sendAfilaxyAlert")
        .call(data)
        .addOnSuccessListener { result ->
            // Notificação enviada com sucesso
        }
        .addOnFailureListener { e ->
            // Trate o erro
        }
}