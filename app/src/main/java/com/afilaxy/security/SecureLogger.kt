package com.afilaxy.security

import android.util.Log

/**
 * Secure logging utility that prevents log injection attacks
 * and provides standardized logging across the Afilaxy application
 */
object SecureLogger {
    
    private const val MAX_LOG_LENGTH = 200
    private const val APP_PREFIX = "Afilaxy"
    
    /**
     * Log debug message with injection prevention
     */
    fun d(tag: String, message: String) {
        Log.d(sanitizeTag(tag), sanitizeMessage(message))
    }
    
    /**
     * Log info message with injection prevention
     */
    fun i(tag: String, message: String) {
        Log.i(sanitizeTag(tag), sanitizeMessage(message))
    }
    
    /**
     * Log warning message with injection prevention
     */
    fun w(tag: String, message: String) {
        Log.w(sanitizeTag(tag), sanitizeMessage(message))
    }
    
    /**
     * Log error message with injection prevention
     */
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.e(sanitizeTag(tag), sanitizeMessage(message), throwable)
        } else {
            Log.e(sanitizeTag(tag), sanitizeMessage(message))
        }
    }
    
    /**
     * Log security event with enhanced protection
     */
    fun security(operation: String, result: String, userId: String? = null) {
        val safeUserId = userId?.let { "user_${it.take(8)}" } ?: "anonymous"
        val message = "Security: ${sanitizeMessage(operation)} - ${sanitizeMessage(result)} - $safeUserId"
        Log.w("${APP_PREFIX}Security", message)
    }
    
    /**
     * Log performance metrics safely
     */
    fun performance(operation: String, duration: Long, success: Boolean) {
        val status = if (success) "SUCCESS" else "FAILED"
        val message = "Performance: ${sanitizeMessage(operation)} - ${duration}ms - $status"
        Log.i("${APP_PREFIX}Performance", message)
    }
    
    /**
     * Sanitize log tag to prevent injection
     */
    private fun sanitizeTag(tag: String): String {
        return "$APP_PREFIX${tag.replace(Regex("[^a-zA-Z0-9_]"), "").take(20)}"
    }
    
    /**
     * Sanitize log message to prevent injection attacks
     */
    private fun sanitizeMessage(message: String): String {
        return message
            .replace("\n", " ") // Remove newlines
            .replace("\r", " ") // Remove carriage returns
            .replace("\t", " ") // Replace tabs with spaces
            .replace("\u0000", "") // Remove null bytes
            .replace(Regex("[\\p{Cntrl}]"), "") // Remove control characters
            .replace(Regex("[\\x00-\\x1F\\x7F]"), "") // Remove ASCII control chars
            .replace("\\x1B\\[[0-9;]*m".toRegex(), "") // Remove ANSI escape sequences
            .replace("\\u001B\\[[;\\d]*m".toRegex(), "") // Remove Unicode escape sequences
            .replace("%n", " ") // Prevent format string attacks
            .replace("%s", " ") // Prevent format string attacks
            .replace("%d", " ") // Prevent format string attacks
            .take(MAX_LOG_LENGTH) // Limit length
    }
    
    /**
     * Log user action with privacy protection
     */
    fun userAction(action: String, userId: String, success: Boolean) {
        val safeUserId = "user_${userId.take(8)}"
        val status = if (success) "SUCCESS" else "FAILED"
        val message = "UserAction: ${sanitizeMessage(action)} - $safeUserId - $status"
        Log.i("${APP_PREFIX}UserAction", message)
    }
    
    /**
     * Log emergency events with high priority
     */
    fun emergency(event: String, location: String? = null) {
        val safeLocation = location?.let { "location_provided" } ?: "no_location"
        val message = "Emergency: ${sanitizeMessage(event)} - $safeLocation"
        Log.w("${APP_PREFIX}Emergency", message)
    }
}