package com.afilaxy.security

import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

object SecurityMonitor {
    
    private val securityEvents = ConcurrentHashMap<String, AtomicInteger>()
    private val suspiciousIPs = ConcurrentHashMap<String, Long>()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    private const val THREAT_THRESHOLD = 5
    private const val BLOCK_DURATION_MS = 300000L // 5 minutes
    
    fun reportSecurityEvent(eventType: String, details: String = "") {
        scope.launch {
            try {
                val count = securityEvents.computeIfAbsent(eventType) { AtomicInteger(0) }
                val newCount = count.incrementAndGet()
                
                SecureLogger.security("SECURITY_MONITOR", "$eventType: $details (count: $newCount)")
                
                if (newCount >= THREAT_THRESHOLD) {
                    handleThreatDetected(eventType, newCount)
                }
            } catch (e: Exception) {
                SecureLogger.e("SecurityMonitor", "Failed to report security event", e)
            }
        }
    }
    
    fun isBlocked(identifier: String): Boolean {
        val blockTime = suspiciousIPs[identifier] ?: return false
        return System.currentTimeMillis() - blockTime < BLOCK_DURATION_MS
    }
    
    private fun handleThreatDetected(eventType: String, count: Int) {
        SecureLogger.security("THREAT_DETECTED", "$eventType exceeded threshold: $count")
        
        when (eventType) {
            "INJECTION_ATTEMPT", "XXE_ATTEMPT", "FILE_UPLOAD_VIOLATION" -> {
                // Block further operations
                suspiciousIPs["current_session"] = System.currentTimeMillis()
            }
        }
    }
    
    fun getSecurityStats(): Map<String, Int> {
        return securityEvents.mapValues { it.value.get() }
    }
    
    fun reset() {
        securityEvents.clear()
        suspiciousIPs.clear()
    }
}