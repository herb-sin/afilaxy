package com.afilaxy.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object SecureCredentials {
    
    private const val PREFS_NAME = "afilaxy_secure_prefs"
    private var encryptedPrefs: SharedPreferences? = null
    
    fun initialize(context: Context) {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            
            encryptedPrefs = EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            SecurityUtils.safeLog("SecureCredentials", "Failed to initialize secure storage: ${e.message}", SecurityUtils.LogLevel.ERROR)
        }
    }
    
    fun getApiKey(): String? {
        return try {
            encryptedPrefs?.getString("api_key", null)
        } catch (e: Exception) {
            SecurityUtils.safeLog("SecureCredentials", "Failed to retrieve API key", SecurityUtils.LogLevel.ERROR)
            null
        }
    }
    
    fun setApiKey(apiKey: String) {
        try {
            encryptedPrefs?.edit()?.putString("api_key", apiKey)?.apply()
        } catch (e: Exception) {
            SecurityUtils.safeLog("SecureCredentials", "Failed to store API key", SecurityUtils.LogLevel.ERROR)
        }
    }
    
    fun getFirebaseConfig(): Map<String, String> {
        return try {
            mapOf(
                "project_id" to (encryptedPrefs?.getString("firebase_project_id", "") ?: ""),
                "app_id" to (encryptedPrefs?.getString("firebase_app_id", "") ?: ""),
                "api_key" to (encryptedPrefs?.getString("firebase_api_key", "") ?: "")
            )
        } catch (e: Exception) {
            SecurityUtils.safeLog("SecureCredentials", "Failed to retrieve Firebase config", SecurityUtils.LogLevel.ERROR)
            emptyMap()
        }
    }
    
    fun clearCredentials() {
        try {
            encryptedPrefs?.edit()?.clear()?.apply()
        } catch (e: Exception) {
            SecurityUtils.safeLog("SecureCredentials", "Failed to clear credentials", SecurityUtils.LogLevel.ERROR)
        }
    }
}