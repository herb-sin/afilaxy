package com.afilaxy.security

/**
 * Real-time security monitoring system
 * Tracks threats and suspicious activities
 */
object SecurityMonitor {
    private val threatCount = mutableMapOf<String, Int>()
    private val blockedIPs = mutableSetOf<String>()
    
    fun logThreat(type: String, details: String) {
        threatCount[type] = threatCount.getOrDefault(type, 0) + 1
        SecureLogger.security("THREAT_DETECTED", "$type: $details")
        
        // Auto-block after multiple threats
        if (threatCount[type]!! > 5) {
            SecureLogger.security("AUTO_BLOCK", "Threat type $type exceeded threshold")
        }
    }
    
    fun reportSuspiciousActivity(activity: String) {
        SecureLogger.security("SUSPICIOUS_ACTIVITY", activity)
    }
    
    fun isBlocked(identifier: String): Boolean {
        return blockedIPs.contains(identifier)
    }
    
    fun getThreatStats(): Map<String, Int> {
        return threatCount.toMap()
    }
}