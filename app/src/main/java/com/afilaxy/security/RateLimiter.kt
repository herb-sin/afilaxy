package com.afilaxy.security

import java.util.concurrent.ConcurrentHashMap

object RateLimiter {
    private val operationTimestamps = ConcurrentHashMap<String, Long>()
    
    // Intervalos mínimos em milissegundos
    private const val EMERGENCY_CREATION_INTERVAL = 60_000L // 1 minuto
    private const val HELPER_ACCEPTANCE_INTERVAL = 20_000L // 20 segundos
    private const val NOTIFICATION_INTERVAL = 12_000L // 12 segundos
    
    fun canCreateEmergency(userId: String): Boolean {
        return checkRateLimit("emergency_$userId", EMERGENCY_CREATION_INTERVAL)
    }
    
    fun canAcceptHelp(userId: String): Boolean {
        return checkRateLimit("accept_$userId", HELPER_ACCEPTANCE_INTERVAL)
    }
    
    fun canSendNotification(userId: String): Boolean {
        return checkRateLimit("notify_$userId", NOTIFICATION_INTERVAL)
    }
    
    private fun checkRateLimit(key: String, intervalMs: Long): Boolean {
        val now = System.currentTimeMillis()
        val lastOperation = operationTimestamps[key]
        
        return if (lastOperation == null || (now - lastOperation) >= intervalMs) {
            operationTimestamps[key] = now
            true
        } else {
            false
        }
    }
    
    fun getRemainingTime(userId: String, operation: String): Long {
        val interval = when (operation) {
            "emergency" -> EMERGENCY_CREATION_INTERVAL
            "accept" -> HELPER_ACCEPTANCE_INTERVAL
            "notify" -> NOTIFICATION_INTERVAL
            else -> 0L
        }
        
        val key = "${operation}_$userId"
        val lastOperation = operationTimestamps[key] ?: return 0L
        val elapsed = System.currentTimeMillis() - lastOperation
        
        return maxOf(0L, interval - elapsed)
    }
}