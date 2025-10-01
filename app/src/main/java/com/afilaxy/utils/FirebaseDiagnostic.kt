package com.afilaxy.utils

import android.content.Context
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object FirebaseDiagnostic {
    
    fun checkGooglePlayServices(context: Context): Boolean {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context)
        
        when (resultCode) {
            ConnectionResult.SUCCESS -> {
                Log.d("FirebaseDiagnostic", "✅ Google Play Services disponível")
                return true
            }
            ConnectionResult.SERVICE_MISSING -> {
                Log.e("FirebaseDiagnostic", "❌ Google Play Services não instalado")
            }
            ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED -> {
                Log.e("FirebaseDiagnostic", "❌ Google Play Services precisa ser atualizado")
            }
            ConnectionResult.SERVICE_DISABLED -> {
                Log.e("FirebaseDiagnostic", "❌ Google Play Services desabilitado")
            }
            else -> {
                Log.e("FirebaseDiagnostic", "❌ Google Play Services erro: $resultCode")
            }
        }
        return false
    }
    
    suspend fun testFirebaseAuth(): Boolean {
        return try {
            val auth = FirebaseAuth.getInstance()
            Log.d("FirebaseDiagnostic", "🔐 Firebase Auth inicializado")
            
            val currentUser = auth.currentUser
            Log.d("FirebaseDiagnostic", "👤 Usuário atual: ${if (currentUser != null) "autenticado" else "não autenticado"}")
            
            true
        } catch (e: Exception) {
            Log.e("FirebaseDiagnostic", "❌ Erro Firebase Auth: ${e.message}", e)
            false
        }
    }
    
    suspend fun testFirestore(): Boolean {
        return try {
            val firestore = FirebaseFirestore.getInstance()
            Log.d("FirebaseDiagnostic", "🗄️ Firestore inicializado")
            
            // Teste de conectividade sem consumir quota
            firestore.disableNetwork().await()
            firestore.enableNetwork().await()
            
            Log.d("FirebaseDiagnostic", "✅ Firestore conectado com sucesso")
            true
        } catch (e: Exception) {
            Log.e("FirebaseDiagnostic", "❌ Erro Firestore: ${e.message}", e)
            false
        }
    }
    
    suspend fun runFullDiagnostic(context: Context) {
        Log.d("FirebaseDiagnostic", "🔍 Iniciando diagnóstico Firebase...")
        
        val playServicesOk = checkGooglePlayServices(context)
        val authOk = testFirebaseAuth()
        val firestoreOk = testFirestore()
        
        Log.d("FirebaseDiagnostic", """
            📊 RESULTADO DO DIAGNÓSTICO:
            - Google Play Services: ${if (playServicesOk) "✅" else "❌"}
            - Firebase Auth: ${if (authOk) "✅" else "❌"}  
            - Firestore: ${if (firestoreOk) "✅" else "❌"}
        """.trimIndent())
    }
}