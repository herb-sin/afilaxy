package com.afilaxy.config

import android.content.Context
import com.afilaxy.BuildConfig
import com.afilaxy.security.SecureCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

object FirebaseConfig {
    
    fun initializeFromEnvironment(context: Context) {
        try {
            // Try environment variables first
            val projectId = getConfigValue("FIREBASE_PROJECT_ID", "")
            val appId = getConfigValue("FIREBASE_APP_ID", "")
            val apiKey = getConfigValue("FIREBASE_API_KEY", "")
            
            if (projectId.isNotEmpty() && appId.isNotEmpty() && apiKey.isNotEmpty()) {
                // Use environment configuration
                val options = FirebaseOptions.Builder()
                    .setProjectId(projectId)
                    .setApplicationId(appId)
                    .setApiKey(apiKey)
                    .setStorageBucket(getConfigValue("FIREBASE_STORAGE_BUCKET", "$projectId.appspot.com"))
                    .build()
                
                if (FirebaseApp.getApps(context).isEmpty()) {
                    FirebaseApp.initializeApp(context, options)
                }
            } else {
                // Fallback to google-services.json
                if (FirebaseApp.getApps(context).isEmpty()) {
                    FirebaseApp.initializeApp(context)
                }
            }
            
            // Store in secure storage for later use
            SecureCredentials.initialize(context)
            
        } catch (e: Exception) {
            // Final fallback - try default initialization
            try {
                if (FirebaseApp.getApps(context).isEmpty()) {
                    FirebaseApp.initializeApp(context)
                }
            } catch (fallbackException: Exception) {
                android.util.Log.e("FirebaseConfig", "All Firebase initialization methods failed: ${fallbackException.message}")
            }
        }
    }
    
    private fun getConfigValue(key: String, default: String): String {
        return when (key) {
            "FIREBASE_PROJECT_ID" -> BuildConfig.FIREBASE_PROJECT_ID.takeIf { it.isNotEmpty() } ?: default
            "FIREBASE_APP_ID" -> BuildConfig.FIREBASE_APP_ID.takeIf { it.isNotEmpty() } ?: default
            "FIREBASE_API_KEY" -> BuildConfig.FIREBASE_API_KEY.takeIf { it.isNotEmpty() } ?: default
            "FIREBASE_STORAGE_BUCKET" -> BuildConfig.FIREBASE_STORAGE_BUCKET.takeIf { it.isNotEmpty() } ?: default
            else -> default
        }
    }
}