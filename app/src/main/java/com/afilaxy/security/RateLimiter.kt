package com.afilaxy.security

import java.util.concurrent.ConcurrentHashMap

object RateLimiter {
    private val operationTimestamps = ConcurrentHashMap<String, Long>(16, 0.75f, 2)
    
    // Intervalos mínimos em milissegundos
    private const val EMERGENCY_CREATION_INTERVAL = 60_000L // 1 minuto
    private const val HELPER_ACCEPTANCE_INTERVAL = 20_000L // 20 segundos
    private const val NOTIFICATION_INTERVAL = 12_000L // 12 segundos
    
    fun canCreateEmergency(userId: String): Boolean {
        val sanitizedUserId = InputSanitizer.sanitizeText(userId)
        if (sanitizedUserId.isEmpty()) {
            SecurityUtils.safeLog("RateLimiter", "Invalid user ID for emergency creation", SecurityUtils.LogLevel.WARN)
            return false
        }
        return checkRateLimit("emergency_$sanitizedUserId", EMERGENCY_CREATION_INTERVAL)
    }
    
    fun canAcceptHelp(userId: String): Boolean {
        val sanitizedUserId = InputSanitizer.sanitizeText(userId)
        if (sanitizedUserId.isEmpty()) return false
        return checkRateLimit("accept_$sanitizedUserId", HELPER_ACCEPTANCE_INTERVAL)
    }
    
    fun canSendNotification(userId: String): Boolean {
        val sanitizedUserId = InputSanitizer.sanitizeText(userId)
        if (sanitizedUserId.isEmpty()) return false
        return checkRateLimit("notify_$sanitizedUserId", NOTIFICATION_INTERVAL)
    }
    
    private fun checkRateLimit(key: String, intervalMs: Long): Boolean {
        return try {
            val now = System.currentTimeMillis()
            val lastOperation = operationTimestamps[key]
            
            if (lastOperation == null || (now - lastOperation) >= intervalMs) {
                operationTimestamps[key] = now
                // Limpeza periódica para evitar vazamento de memória
                if (operationTimestamps.size > 100) {
                    cleanupOldEntries(now)
                }
                true
            } else {
                SecurityUtils.safeLog("RateLimiter", "Rate limit exceeded for key: $key", SecurityUtils.LogLevel.DEBUG)
                false
            }
        } catch (e: Exception) {
            SecurityUtils.safeLog("RateLimiter", "Error checking rate limit: ${e.message}", SecurityUtils.LogLevel.ERROR)
            false
        }
    }
    
    private fun cleanupOldEntries(currentTime: Long) {
        val cutoffTime = currentTime - 3600000L // 1 hora
        val iterator = operationTimestamps.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (entry.value < cutoffTime) {
                iterator.remove()
            }
        }
    }
    
    // For testing purposes only
    internal fun clearAll() {
        operationTimestamps.clear()
    }
    
    // Block user for security reasons
    fun blockUser(userId: String, durationMs: Long) {
        val sanitizedUserId = InputSanitizer.sanitizeText(userId)
        if (sanitizedUserId.isEmpty()) return
        
        val blockUntil = System.currentTimeMillis() + durationMs
        operationTimestamps["blocked_$sanitizedUserId"] = blockUntil
        
        SecurityUtils.safeLog(
            "RateLimiter", 
            "User blocked for ${durationMs / 1000} seconds", 
            SecurityUtils.LogLevel.WARN
        )
    }
    
    fun isUserBlocked(userId: String): Boolean {
        val sanitizedUserId = InputSanitizer.sanitizeText(userId)
        if (sanitizedUserId.isEmpty()) return true
        
        val blockUntil = operationTimestamps["blocked_$sanitizedUserId"] ?: return false
        return System.currentTimeMillis() < blockUntil
    }
    
    fun getRemainingTime(userId: String, operation: String): Long {
        return try {
            val sanitizedUserId = InputSanitizer.sanitizeText(userId)
            val sanitizedOperation = InputSanitizer.sanitizeText(operation)
            
            if (sanitizedUserId.isEmpty() || sanitizedOperation.isEmpty()) return 0L
            
            val interval = when (sanitizedOperation) {
                "emergency" -> EMERGENCY_CREATION_INTERVAL
                "accept" -> HELPER_ACCEPTANCE_INTERVAL
                "notify" -> NOTIFICATION_INTERVAL
                else -> 0L
            }
            
            val key = "${sanitizedOperation}_$sanitizedUserId"
            val lastOperation = operationTimestamps[key] ?: return 0L
            val elapsed = System.currentTimeMillis() - lastOperation
            
            maxOf(0L, interval - elapsed)
        } catch (e: Exception) {
            SecurityUtils.safeLog("RateLimiter", "Error getting remaining time: ${e.message}", SecurityUtils.LogLevel.ERROR)
            0L
        }
    }
}