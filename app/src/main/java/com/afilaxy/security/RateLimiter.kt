package com.afilaxy.security

import java.util.concurrent.ConcurrentHashMap

/**
 * Rate limiter for Afilaxy application
 * Prevents abuse and ensures fair usage of resources
 */
object RateLimiter {
    
    private val emergencyRequests = ConcurrentHashMap<String, MutableList<Long>>()
    private val notificationRequests = ConcurrentHashMap<String, MutableList<Long>>()
    private val generalRequests = ConcurrentHashMap<String, MutableList<Long>>()
    
    // Rate limits configuration
    private const val EMERGENCY_MAX_REQUESTS = 3 // Max emergency requests
    private const val EMERGENCY_TIME_WINDOW_MS = 10 * 60 * 1000L // 10 minutes
    
    private const val NOTIFICATION_MAX_REQUESTS = 10 // Max notifications
    private const val NOTIFICATION_TIME_WINDOW_MS = 5 * 60 * 1000L // 5 minutes
    
    private const val GENERAL_MAX_REQUESTS = 100 // Max general requests
    private const val GENERAL_TIME_WINDOW_MS = 60 * 1000L // 1 minute
    
    /**
     * Check if user can create emergency request
     */
    fun canCreateEmergency(userId: String): Boolean {
        return checkRateLimit(
            userId,
            emergencyRequests,
            EMERGENCY_MAX_REQUESTS,
            EMERGENCY_TIME_WINDOW_MS,
            "EMERGENCY_RATE_LIMIT"
        )
    }
    
    /**
     * Check if user can send notification
     */
    fun canSendNotification(userId: String): Boolean {
        return checkRateLimit(
            userId,
            notificationRequests,
            NOTIFICATION_MAX_REQUESTS,
            NOTIFICATION_TIME_WINDOW_MS,
            "NOTIFICATION_RATE_LIMIT"
        )
    }
    
    /**
     * Check if user can make general request
     */
    fun canMakeGeneralRequest(userId: String): Boolean {
        return checkRateLimit(
            userId,
            generalRequests,
            GENERAL_MAX_REQUESTS,
            GENERAL_TIME_WINDOW_MS,
            "GENERAL_RATE_LIMIT"
        )
    }
    
    /**
     * Check if user can perform specific action
     */
    fun canPerformAction(userId: String, action: String): Boolean {
        return when (action) {
            "accept" -> canMakeGeneralRequest(userId)
            "emergency" -> canCreateEmergency(userId)
            "notification" -> canSendNotification(userId)
            else -> canMakeGeneralRequest(userId)
        }
    }
    
    /**
     * Get remaining time for rate limit
     */
    fun getRemainingTime(userId: String, action: String): Long {
        val currentTime = System.currentTimeMillis()
        val requests = when (action) {
            "accept" -> generalRequests[userId]
            "emergency" -> emergencyRequests[userId]
            "notification" -> notificationRequests[userId]
            else -> generalRequests[userId]
        }
        
        val timeWindow = when (action) {
            "accept" -> GENERAL_TIME_WINDOW_MS
            "emergency" -> EMERGENCY_TIME_WINDOW_MS
            "notification" -> NOTIFICATION_TIME_WINDOW_MS
            else -> GENERAL_TIME_WINDOW_MS
        }
        
        return requests?.firstOrNull()?.let { oldestRequest ->
            val elapsed = currentTime - oldestRequest
            maxOf(0, timeWindow - elapsed)
        } ?: 0L
    }
    
    /**
     * Generic rate limit checker
     */
    private fun checkRateLimit(
        userId: String,
        requestMap: ConcurrentHashMap<String, MutableList<Long>>,
        maxRequests: Int,
        timeWindowMs: Long,
        limitType: String
    ): Boolean {
        val currentTime = System.currentTimeMillis()
        val userRequests = requestMap.getOrPut(userId) { mutableListOf() }
        
        synchronized(userRequests) {
            // Remove old requests outside time window
            userRequests.removeAll { currentTime - it > timeWindowMs }
            
            // Check if user has exceeded rate limit
            if (userRequests.size >= maxRequests) {
                SecureLogger.security(limitType, "RATE_LIMIT_EXCEEDED", userId)
                return false
            }
            
            // Add current request
            userRequests.add(currentTime)
            return true
        }
    }
    
    /**
     * Reset rate limits for a user (admin function)
     */
    fun resetUserLimits(userId: String) {
        emergencyRequests.remove(userId)
        notificationRequests.remove(userId)
        generalRequests.remove(userId)
        SecureLogger.security("RATE_LIMIT_RESET", "SUCCESS", userId)
    }
    
    /**
     * Clean up old entries to prevent memory leaks
     */
    fun cleanup() {
        val currentTime = System.currentTimeMillis()
        
        cleanupMap(emergencyRequests, currentTime, EMERGENCY_TIME_WINDOW_MS)
        cleanupMap(notificationRequests, currentTime, NOTIFICATION_TIME_WINDOW_MS)
        cleanupMap(generalRequests, currentTime, GENERAL_TIME_WINDOW_MS)
        
        SecureLogger.d("RateLimiter", "Cleanup completed")
    }
    
    /**
     * Clean up a specific request map
     */
    private fun cleanupMap(
        requestMap: ConcurrentHashMap<String, MutableList<Long>>,
        currentTime: Long,
        timeWindowMs: Long
    ) {
        val usersToRemove = mutableListOf<String>()
        
        requestMap.forEach { (userId, requests) ->
            synchronized(requests) {
                requests.removeAll { currentTime - it > timeWindowMs }
                if (requests.isEmpty()) {
                    usersToRemove.add(userId)
                }
            }
        }
        
        usersToRemove.forEach { requestMap.remove(it) }
    }
    
    /**
     * Get rate limit status for a user
     */
    fun getRateLimitStatus(userId: String): RateLimitStatus {
        val currentTime = System.currentTimeMillis()
        
        val emergencyCount = emergencyRequests[userId]?.let { requests ->
            synchronized(requests) {
                requests.removeAll { currentTime - it > EMERGENCY_TIME_WINDOW_MS }
                requests.size
            }
        } ?: 0
        
        val notificationCount = notificationRequests[userId]?.let { requests ->
            synchronized(requests) {
                requests.removeAll { currentTime - it > NOTIFICATION_TIME_WINDOW_MS }
                requests.size
            }
        } ?: 0
        
        val generalCount = generalRequests[userId]?.let { requests ->
            synchronized(requests) {
                requests.removeAll { currentTime - it > GENERAL_TIME_WINDOW_MS }
                requests.size
            }
        } ?: 0
        
        return RateLimitStatus(
            emergencyRequests = emergencyCount,
            emergencyLimit = EMERGENCY_MAX_REQUESTS,
            notificationRequests = notificationCount,
            notificationLimit = NOTIFICATION_MAX_REQUESTS,
            generalRequests = generalCount,
            generalLimit = GENERAL_MAX_REQUESTS
        )
    }
    
    data class RateLimitStatus(
        val emergencyRequests: Int,
        val emergencyLimit: Int,
        val notificationRequests: Int,
        val notificationLimit: Int,
        val generalRequests: Int,
        val generalLimit: Int
    ) {
        val canCreateEmergency: Boolean get() = emergencyRequests < emergencyLimit
        val canSendNotification: Boolean get() = notificationRequests < notificationLimit
        val canMakeGeneralRequest: Boolean get() = generalRequests < generalLimit
    }
}