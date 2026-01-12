package com.afilaxy.config

import com.afilaxy.security.SecureLogger

object AppConfig {
    const val IS_DEBUG = true
    const val ENABLE_CRASH_REPORTING = false
    const val MOCK_FIREBASE = false
    const val HELPER_SEARCH_RADIUS_KM = 0.3
    const val NOTIFICATION_TIMEOUT_MS = 30000L
    
    fun getConfigValue(key: String, defaultValue: Any): Any {
        return try {
            when (key) {
                "IS_DEBUG" -> IS_DEBUG
                "ENABLE_CRASH_REPORTING" -> ENABLE_CRASH_REPORTING
                "MOCK_FIREBASE" -> MOCK_FIREBASE
                "HELPER_SEARCH_RADIUS_KM" -> HELPER_SEARCH_RADIUS_KM
                "NOTIFICATION_TIMEOUT_MS" -> NOTIFICATION_TIMEOUT_MS
                else -> defaultValue
            }
        } catch (e: Exception) {
            SecureLogger.w("AppConfig", "Error getting config value: $key")
            defaultValue
        }
    }
}