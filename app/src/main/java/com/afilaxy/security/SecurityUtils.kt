package com.afilaxy.security

import android.util.Log

object SecurityUtils {
    
    enum class LogLevel { DEBUG, INFO, WARN, ERROR, SECURITY }
    
    fun safeLog(tag: String, message: String, level: LogLevel) {
        val sanitizedTag = sanitizeForLogging(tag)
        val sanitizedMessage = sanitizeForLogging(message)
        
        when (level) {
            LogLevel.DEBUG -> Log.d(sanitizedTag, sanitizedMessage)
            LogLevel.INFO -> Log.i(sanitizedTag, sanitizedMessage)
            LogLevel.WARN -> Log.w(sanitizedTag, sanitizedMessage)
            LogLevel.ERROR -> Log.e(sanitizedTag, sanitizedMessage)
            LogLevel.SECURITY -> Log.e("SECURITY_$sanitizedTag", sanitizedMessage)
        }
    }
    
    private fun sanitizeForLogging(input: String): String {
        return input
            .replace("\n", "_")
            .replace("\r", "_")
            .replace("\t", "_")
            .replace("\u0000", "_")
            .replace(Regex("[\\p{Cntrl}]"), "_")
            .replace("%n", "_")
            .replace("%s", "_")
            .replace("%d", "_")
            .take(100)
            .filter { it.isLetterOrDigit() || it in "_-. " }
            .ifBlank { "SANITIZED" }
    }
    
    fun validateOperation(operation: String): Boolean {
        return try {
            AuthGuard.isUserAuthenticated() && 
            operation.isNotBlank() && 
            operation.length <= 50 &&
            operation.matches(Regex("^[a-zA-Z0-9_]+$"))
        } catch (e: Exception) {
            false
        }
    }
    
    fun isValidCoordinate(lat: Double, lon: Double): Boolean {
        return lat in -90.0..90.0 && 
               lon in -180.0..180.0 &&
               !lat.isNaN() && !lat.isInfinite() &&
               !lon.isNaN() && !lon.isInfinite()
    }
}