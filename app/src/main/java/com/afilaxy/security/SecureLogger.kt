package com.afilaxy.security

import android.util.Log

object SecureLogger {
    
    private const val MAX_LOG_LENGTH = 4000
    
    fun d(tag: String, message: String) {
        val sanitizedTag = sanitize(tag)
        val sanitizedMessage = sanitize(message)
        Log.d(sanitizedTag, sanitizedMessage)
    }
    
    fun i(tag: String, message: String) {
        val sanitizedTag = sanitize(tag)
        val sanitizedMessage = sanitize(message)
        Log.i(sanitizedTag, sanitizedMessage)
    }
    
    fun w(tag: String, message: String) {
        val sanitizedTag = sanitize(tag)
        val sanitizedMessage = sanitize(message)
        Log.w(sanitizedTag, sanitizedMessage)
    }
    
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        val sanitizedTag = sanitize(tag)
        val sanitizedMessage = sanitize(message)
        if (throwable != null) {
            Log.e(sanitizedTag, sanitizedMessage, throwable)
        } else {
            Log.e(sanitizedTag, sanitizedMessage)
        }
    }
    
    fun security(tag: String, message: String) {
        try {
            if (!AuthGuard.isUserAuthenticated()) {
                return // Don't log security events for unauthenticated users
            }
            val sanitizedTag = sanitizeSecurityTag(tag)
            val sanitizedMessage = sanitizeSecurityMessage(message)
            Log.e("SECURITY_$sanitizedTag", sanitizedMessage)
        } catch (e: Exception) {
            // Fail silently to prevent log injection through exception messages
        }
    }
    
    fun performance(operation: String, duration: Long, success: Boolean) {
        val sanitizedOperation = sanitize(operation)
        Log.i("PERFORMANCE", "$sanitizedOperation: ${duration}ms success: $success")
    }
    
    private fun sanitize(input: String): String {
        return try {
            input
                .replace("\n", "_")
                .replace("\r", "_")
                .replace("\t", "_")
                .replace("\u0000", "_")
                .replace(Regex("[\\p{Cntrl}]"), "_")
                .replace("%n", "_")
                .replace("%s", "_")
                .replace("%d", "_")
                .replace("%x", "_")
                .replace("%c", "_")
                .replace("\\x", "_")
                .replace("\\u", "_")
                .replace("\\n", "_")
                .replace("\\r", "_")
                .replace("\\t", "_")
                .take(MAX_LOG_LENGTH)
                .filter { it.isLetterOrDigit() || it in "_-. :()[]{}/" }
                .ifBlank { "SANITIZED" }
        } catch (e: Exception) {
            "SANITIZED"
        }
    }
    
    private fun sanitizeSecurityTag(tag: String): String {
        return try {
            tag.filter { it.isLetterOrDigit() || it == '_' }
                .take(50)
                .ifBlank { "SECURITY" }
        } catch (e: Exception) {
            "SECURITY"
        }
    }
    
    private fun sanitizeSecurityMessage(message: String): String {
        return try {
            message
                .replace(Regex("[\\r\\n\\t\\x00-\\x1F\\x7F-\\x9F]"), "_")
                .replace(Regex("%[nsdxc]"), "_")
                .replace(Regex("\\\\[nrtx]"), "_")
                .replace(Regex("\\\\u[0-9a-fA-F]{4}"), "_")
                .filter { it.code in 32..126 || it == '_' }
                .take(500)
                .ifBlank { "SECURITY_EVENT" }
        } catch (e: Exception) {
            "SECURITY_EVENT"
        }
    }
}