package com.afilaxy

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import android.content.Context

fun saveFcmTokenToFirestore(context: Context) {
    val user = FirebaseAuth.getInstance().currentUser
    
    // Verificação crítica de autenticação
    if (user == null) {
        android.util.Log.e("FirebaseUtils", "Tentativa de salvar token FCM sem autenticação")
        return
    }
    
    if (!user.isEmailVerified) {
        android.util.Log.e("FirebaseUtils", "Tentativa de salvar token FCM com email não verificado")
        return
    }
    
    FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
        if (task.isSuccessful) {
            val token = task.result
            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(user.uid)
                .update("fcmToken", token)
                .addOnSuccessListener {
                    android.util.Log.d("FirebaseUtils", "Token FCM salvo com sucesso")
                }
                .addOnFailureListener { e ->
                    android.util.Log.e("FirebaseUtils", "Erro ao salvar token FCM: ${e.message}")
                }
        }
    }
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