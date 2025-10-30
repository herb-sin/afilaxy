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
                // Firebase auto-initializes, but we can ensure it's ready
                FirebaseApp.initializeApp(this@AfilaxyApplication)
            } catch (e: Exception) {
                // Firebase already initialized or error - continue
            }
        }
    }
}