package com.afilaxy.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.afilaxy.domain.repository.IPreferencesRepository

class PreferencesRepositoryImpl(context: Context) : IPreferencesRepository {
    
    private val prefs: SharedPreferences = try {
        EncryptedSharedPreferences.create(
            "afilaxy_prefs",
            MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        android.util.Log.w("PreferencesRepository", "Fallback to regular SharedPreferences: ${e.javaClass.simpleName}")
        context.getSharedPreferences("afilaxy_prefs", Context.MODE_PRIVATE)
    }
    
    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return prefs.getBoolean(key, defaultValue)
    }
    
    override fun putBoolean(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }
    
    override fun getString(key: String, defaultValue: String?): String? {
        return prefs.getString(key, defaultValue)
    }
    
    override fun putString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }
}