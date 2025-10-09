package com.afilaxy

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseTest {
    fun testConnection() {
        val auth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()
        
        Log.d("FirebaseTest", "Auth instance: ${auth != null}")
        Log.d("FirebaseTest", "Firestore instance: ${firestore != null}")
        
        // Teste de conexão
        firestore.collection("test").document("connection")
            .set(mapOf("timestamp" to System.currentTimeMillis()))
            .addOnSuccessListener {
                Log.d("FirebaseTest", "✅ Firestore conectado!")
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseTest", "❌ Erro Firestore: ${e.message}")
            }
    }
}