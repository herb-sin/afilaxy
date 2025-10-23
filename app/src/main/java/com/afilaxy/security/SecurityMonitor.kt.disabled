package com.afilaxy.security

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

object SecurityMonitor {
    
    private val suspiciousActivities = ConcurrentHashMap<String, AtomicInteger>()
    private val blockedIPs = mutableSetOf<String>()
    
    // Threat detection thresholds
    private const val MAX_FAILED_ATTEMPTS = 5
    private const val MAX_RAPID_REQUESTS = 10
    private const val MONITORING_WINDOW_MS = 300_000L // 5 minutes
    
    fun reportSuspiciousActivity(userId: String, activityType: String, details: String = "") {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val key = "${userId}_${activityType}"
                val count = suspiciousActivities.computeIfAbsent(key) { AtomicInteger(0) }
                val newCount = count.incrementAndGet()
                
                SecurityUtils.safeLog(
                    "SecurityMonitor", 
                    "Suspicious activity detected: $activityType for user $userId (count: $newCount)",
                    SecurityUtils.LogLevel.WARN
                )
                
                if (newCount >= MAX_FAILED_ATTEMPTS) {
                    handleSecurityThreat(userId, activityType, newCount)
                }
                
                // Log to secure storage for analysis
                logSecurityEvent(userId, activityType, details, newCount)
                
            } catch (e: Exception) {
                SecurityUtils.safeLog("SecurityMonitor", "Error reporting activity: ${e.message}", SecurityUtils.LogLevel.ERROR)
            }
        }
    }
    
    private fun handleSecurityThreat(userId: String, activityType: String, count: Int) {
        SecurityUtils.safeLog(
            "SecurityMonitor",
            "Security threat detected: $activityType for user $userId (count: $count)",
            SecurityUtils.LogLevel.ERROR
        )
        
        when (activityType) {
            "failed_login" -> {
                // Temporarily block user
                RateLimiter.blockUser(userId, 900_000L) // 15 minutes
            }
            "injection_attempt" -> {
                // Block immediately and log for investigation
                RateLimiter.blockUser(userId, 3600_000L) // 1 hour
                reportCriticalThreat(userId, activityType)
            }
            "rapid_requests" -> {
                // Rate limit more aggressively
                RateLimiter.blockUser(userId, 300_000L) // 5 minutes
            }
        }
    }
    
    private fun reportCriticalThreat(userId: String, activityType: String) {
        // In production, this would send alerts to security team
        SecurityUtils.safeLog(
            "SecurityMonitor",
            "CRITICAL THREAT: $activityType from user $userId requires immediate attention",
            SecurityUtils.LogLevel.ERROR
        )
    }
    
    private fun logSecurityEvent(userId: String, activityType: String, details: String, count: Int) {
        // Store in secure local storage for later analysis
        val timestamp = System.currentTimeMillis()
        val event = SecurityEvent(
            userId = InputSanitizer.sanitizeText(userId),
            activityType = InputSanitizer.sanitizeText(activityType),
            details = InputSanitizer.sanitizeText(details),
            count = count,
            timestamp = timestamp
        )
        
        // In production, send to secure logging service
        SecurityUtils.safeLog(
            "SecurityEvent",
            "Event: ${event.activityType}, User: ${event.userId}, Count: ${event.count}",
            SecurityUtils.LogLevel.INFO
        )
    }
    
    fun checkUserSecurity(userId: String): SecurityStatus {
        val sanitizedUserId = InputSanitizer.sanitizeText(userId)
        if (sanitizedUserId.isEmpty()) return SecurityStatus.BLOCKED
        
        val failedLogins = suspiciousActivities["${sanitizedUserId}_failed_login"]?.get() ?: 0
        val injectionAttempts = suspiciousActivities["${sanitizedUserId}_injection_attempt"]?.get() ?: 0
        
        return when {
            injectionAttempts > 0 -> SecurityStatus.BLOCKED
            failedLogins >= MAX_FAILED_ATTEMPTS -> SecurityStatus.RESTRICTED
            failedLogins >= 3 -> SecurityStatus.MONITORED
            else -> SecurityStatus.NORMAL
        }
    }
    
    fun cleanupOldEntries() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val cutoffTime = System.currentTimeMillis() - MONITORING_WINDOW_MS
                // In production, implement proper cleanup based on timestamps
                if (suspiciousActivities.size > 1000) {
                    suspiciousActivities.clear()
                }
            } catch (e: Exception) {
                SecurityUtils.safeLog("SecurityMonitor", "Cleanup error: ${e.message}", SecurityUtils.LogLevel.ERROR)
            }
        }
    }
    
    enum class SecurityStatus {
        NORMAL, MONITORED, RESTRICTED, BLOCKED
    }
    
    data class SecurityEvent(
        val userId: String,
        val activityType: String,
        val details: String,
        val count: Int,
        val timestamp: Long
    )
}