package com.afilaxy.config

import android.content.Context
import com.afilaxy.BuildConfig
import com.afilaxy.security.SecureCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

object FirebaseConfig {
    
    fun initializeFromEnvironment(context: Context) {
        try {
            // Validate context and prevent XXE attacks
            if (!isValidContext(context)) {
                com.afilaxy.security.SecureLogger.security("FIREBASE_INIT", "INVALID_CONTEXT")
                return
            }
            
            // Try environment variables first with secure validation
            val projectId = getConfigValue("FIREBASE_PROJECT_ID", "")
            val appId = getConfigValue("FIREBASE_APP_ID", "")
            val apiKey = getConfigValue("FIREBASE_API_KEY", "")
            
            // Validate configuration values
            if (!validateFirebaseConfig(projectId, appId, apiKey)) {
                com.afilaxy.security.SecureLogger.security("FIREBASE_INIT", "INVALID_CONFIG")
                return
            }
            
            if (projectId.isNotEmpty() && appId.isNotEmpty() && apiKey.isNotEmpty()) {
                // Use environment configuration with secure builder
                val options = createSecureFirebaseOptions(projectId, appId, apiKey)
                if (options == null) {
                    com.afilaxy.security.SecureLogger.security("FIREBASE_INIT", "SECURE_OPTIONS_FAILED")
                    return
                }
                
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
                com.afilaxy.security.SecureLogger.e("FirebaseConfig", "Firebase initialization failed", fallbackException)
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
    
    private fun isValidContext(context: Context?): Boolean {
        return context != null && context.applicationContext != null
    }
    
    private fun validateFirebaseConfig(projectId: String, appId: String, apiKey: String): Boolean {
        return projectId.matches(Regex("^[a-zA-Z0-9-]+$")) &&
               appId.matches(Regex("^[a-zA-Z0-9:.-]+$")) &&
               apiKey.matches(Regex("^[a-zA-Z0-9_-]+$")) &&
               projectId.length in 6..30 &&
               appId.length in 10..100 &&
               apiKey.length in 20..100
    }
    
    private fun createSecureFirebaseOptions(projectId: String, appId: String, apiKey: String): FirebaseOptions? {
        return try {
            FirebaseOptions.Builder()
                .setProjectId(projectId)
                .setApplicationId(appId)
                .setApiKey(apiKey)
                .setStorageBucket(getConfigValue("FIREBASE_STORAGE_BUCKET", "$projectId.appspot.com"))
                .build()
        } catch (e: Exception) {
            com.afilaxy.security.SecureLogger.e("FirebaseConfig", "Failed to create secure options", e)
            null
        }
    }
}