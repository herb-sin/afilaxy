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
            .replace("\u0000", "") // Null byte removal
            .replace(Regex("[\\p{Cntrl}]"), "") // Control characters
            .take(200) // Reduced length limit
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
        // Prevent injection by using safe formatting
        return "lat=${"%.6f".format(lat)}, lon=${"%.6f".format(lon)}"
    }
    
    enum class LogLevel {
        DEBUG, INFO, WARN, ERROR
    }
    
    // Safe operation validation - prevents code injection
    fun validateOperation(operationName: String): Boolean {
        // Strict whitelist validation - no sanitization, only exact matches
        val allowedOperations = setOf(
            "location_update", "emergency_request", "notification_send",
            "user_profile_update", "helper_search", "auth_check",
            "backup_data", "restore_data", "app_init"
        )
        
        return try {
            // Direct comparison without sanitization to prevent bypass
            if (!allowedOperations.contains(operationName)) {
                safeLog("SecurityUtils", "Invalid operation attempted: blocked", LogLevel.WARN)
                return false
            }
            
            // Only require auth for sensitive operations
            val sensitiveOperations = setOf("emergency_request", "helper_search", "backup_data")
            if (sensitiveOperations.contains(operationName) && !AuthGuard.isUserAuthenticated()) {
                safeLog("SecurityUtils", "Operation denied - authentication required", LogLevel.WARN)
                return false
            }
            
            true
        } catch (e: Exception) {
            safeLog("SecurityUtils", "Validation failed", LogLevel.ERROR)
            false
        }
    }
    
    // Strict input validation - no sanitization, only validation
    fun isValidInput(input: String): Boolean {
        return input.length <= 255 && 
               input.matches(Regex("^[a-zA-Z0-9@._-]+$")) &&
               !input.contains("--") && // SQL comment prevention
               !input.contains("/*") && // SQL comment prevention
               !input.contains("*/") &&
               !input.contains(";")     // SQL injection prevention
    }
    
    // Safe input for logging only
    private fun sanitizeForLogging(input: String): String {
        return input
            .replace(Regex("[^a-zA-Z0-9@._-\\s]"), "_")
            .take(100)
    }
    
    // Validate coordinates
    fun isValidCoordinate(lat: Double, lon: Double): Boolean {
        return lat in -90.0..90.0 && lon in -180.0..180.0 &&
               !lat.isNaN() && !lat.isInfinite() &&
               !lon.isNaN() && !lon.isInfinite()
    }
    
    // Validate critical parameters to prevent injection
    fun validateCriticalParams(params: Map<String, Any?>): Boolean {
        return try {
            params.all { (key, value) ->
                when {
                    key.length > 50 -> false
                    !key.matches(Regex("^[a-zA-Z_][a-zA-Z0-9_]*$")) -> false
                    value is String && value.length > 1000 -> false
                    value is String && !isValidInput(value) -> false
                    else -> true
                }
            }
        } catch (e: Exception) {
            safeLog("SecurityUtils", "Parameter validation failed", LogLevel.ERROR)
            false
        }
    }
}