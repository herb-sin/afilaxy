package com.afilaxy.security

import org.junit.Test
import org.junit.Assert.*
import org.junit.Before

class RateLimiterTest {

    @Before
    fun setup() {
        // Clear any existing rate limits
        RateLimiter.clearAll()
    }

    @Test
    fun `canCreateEmergency should allow first request`() {
        assertTrue(RateLimiter.canCreateEmergency("user123"))
    }

    @Test
    fun `canCreateEmergency should block rapid requests`() {
        val userId = "user123"
        assertTrue(RateLimiter.canCreateEmergency(userId))
        assertFalse(RateLimiter.canCreateEmergency(userId))
    }

    @Test
    fun `canAcceptHelp should allow first request`() {
        assertTrue(RateLimiter.canAcceptHelp("helper123"))
    }

    @Test
    fun `canSendNotification should allow first request`() {
        assertTrue(RateLimiter.canSendNotification("user123"))
    }

    @Test
    fun `getRemainingTime should return correct time`() {
        val userId = "user123"
        RateLimiter.canCreateEmergency(userId)
        val remaining = RateLimiter.getRemainingTime(userId, "emergency")
        assertTrue(remaining > 0)
        assertTrue(remaining <= 60000) // 1 minute max
    }

    @Test
    fun `should handle invalid user IDs`() {
        assertFalse(RateLimiter.canCreateEmergency(""))
        assertFalse(RateLimiter.canCreateEmergency("user\$injection"))
    }

    @Test
    fun `getRemainingTime should handle invalid operations`() {
        val remaining = RateLimiter.getRemainingTime("user123", "invalid_op")
        assertEquals(0L, remaining)
    }
}