package com.afilaxy.security

import android.util.Log

object SecurityUtils {
    
    // Safe logging to prevent log injection
    fun safeLog(tag: String, message: String, level: LogLevel = LogLevel.DEBUG) {
        val safeTag = sanitizeLogInput(tag)
        val safeMessage = sanitizeLogInput(message)
        
        when (level) {
            LogLevel.DEBUG -> Log.d(safeTag, safeMessage)
            LogLevel.INFO -> Log.i(safeTag, safeMessage)
            LogLevel.WARN -> Log.w(safeTag, safeMessage)
            LogLevel.ERROR -> Log.e(safeTag, safeMessage)
        }
    }
    
    private fun sanitizeLogInput(input: String): String {
        return input
            .replace("\n", " ")
            .replace("\r", " ")
            .replace("\t", " ")
            .take(500) // Limit log message length
    }
    
    // Validate authentication for all operations
    fun requireAuthentication(operation: String): Boolean {
        if (!AuthGuard.isUserAuthenticated()) {
            safeLog("SecurityUtils", "Operation '$operation' denied - user not authenticated", LogLevel.WARN)
            return false
        }
        return true
    }
    
    // Safe string formatting for logs
    fun formatSafeCoordinates(lat: Double, lon: Double): String {
        if (lat < -90 || lat > 90 || lon < -180 || lon > 180) {
            return "invalid_coordinates"
        }
        return "lat=${String.format("%.6f", lat)}, lon=${String.format("%.6f", lon)}"
    }
    
    enum class LogLevel {
        DEBUG, INFO, WARN, ERROR
    }
    
    // Safe operation validation - prevents code injection
    fun validateOperation(operationName: String): Boolean {
        val sanitizedOperation = sanitizeInput(operationName)
        val allowedOperations = setOf(
            "location_update", "emergency_request", "notification_send",
            "user_profile_update", "helper_search", "auth_check",
            "backup_data", "restore_data", "app_init"
        )
        
        return try {
            if (!allowedOperations.contains(sanitizedOperation)) {
                safeLog("SecurityUtils", "Invalid operation attempted", LogLevel.WARN)
                return false
            }
            
            // Only require auth for sensitive operations
            val sensitiveOperations = setOf("emergency_request", "helper_search", "backup_data")
            if (sensitiveOperations.contains(sanitizedOperation) && !AuthGuard.isUserAuthenticated()) {
                safeLog("SecurityUtils", "Operation denied - authentication required", LogLevel.WARN)
                return false
            }
            
            true
        } catch (e: Exception) {
            safeLog("SecurityUtils", "Validation failed", LogLevel.ERROR)
            false
        }
    }
    
    // Input sanitization for database queries
    fun sanitizeInput(input: String): String {
        return input
            .replace(Regex("[^a-zA-Z0-9@._-]"), "")
            .take(255)
    }
    
    // Validate coordinates
    fun isValidCoordinate(lat: Double, lon: Double): Boolean {
        return lat in -90.0..90.0 && lon in -180.0..180.0
    }
}