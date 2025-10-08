package com.afilaxy.security

import org.junit.Test
import org.junit.Assert.*

class SecurityUtilsTest {

    @Test
    fun `sanitizeInput should remove dangerous characters`() {
        val input = "test\${}[]();'\"\\/*<>="
        val result = SecurityUtils.sanitizeInput(input)
        assertEquals("test", result)
    }

    @Test
    fun `sanitizeInput should limit length to 255 characters`() {
        val input = "a".repeat(300)
        val result = SecurityUtils.sanitizeInput(input)
        assertEquals(255, result.length)
    }

    @Test
    fun `isValidCoordinate should validate latitude and longitude`() {
        assertTrue(SecurityUtils.isValidCoordinate(0.0, 0.0))
        assertTrue(SecurityUtils.isValidCoordinate(-90.0, -180.0))
        assertTrue(SecurityUtils.isValidCoordinate(90.0, 180.0))
        
        assertFalse(SecurityUtils.isValidCoordinate(-91.0, 0.0))
        assertFalse(SecurityUtils.isValidCoordinate(91.0, 0.0))
        assertFalse(SecurityUtils.isValidCoordinate(0.0, -181.0))
        assertFalse(SecurityUtils.isValidCoordinate(0.0, 181.0))
    }

    @Test
    fun `validateOperation should only allow whitelisted operations`() {
        assertTrue(SecurityUtils.validateOperation("location_update"))
        assertTrue(SecurityUtils.validateOperation("emergency_request"))
        assertFalse(SecurityUtils.validateOperation("malicious_operation"))
        assertFalse(SecurityUtils.validateOperation(""))
    }

    @Test
    fun `formatSafeCoordinates should handle invalid coordinates`() {
        val result = SecurityUtils.formatSafeCoordinates(91.0, 0.0)
        assertEquals("invalid_coordinates", result)
    }

    @Test
    fun `formatSafeCoordinates should format valid coordinates`() {
        val result = SecurityUtils.formatSafeCoordinates(-23.550520, -46.633308)
        assertTrue(result.contains("lat=-23.550520"))
        assertTrue(result.contains("lon=-46.633308"))
    }
}