package com.afilaxy

import android.app.Application
import com.afilaxy.performance.AnrOptimizer
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AfilaxyApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize heavy operations in background to prevent ANR
        AnrOptimizer.executeAsync {
            initializeServices()
        }
    }
    
    private suspend fun initializeServices() {
        withContext(Dispatchers.IO) {
            try {
                // Garantir que Firebase está inicializado
                val app = FirebaseApp.initializeApp(this@AfilaxyApplication)
                if (app != null) {
                    android.util.Log.d("AfilaxyApp", "Firebase inicializado com sucesso")
                } else {
                    android.util.Log.w("AfilaxyApp", "Firebase já estava inicializado")
                }
            } catch (e: Exception) {
                android.util.Log.e("AfilaxyApp", "Erro ao inicializar Firebase: ${e.message}")
            }
        }
    }
}