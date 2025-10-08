package com.afilaxy.security

import org.junit.Test
import org.junit.Assert.*

class InputSanitizerTest {

    @Test
    fun `sanitizeEmail should validate and clean email`() {
        assertEquals("test@example.com", InputSanitizer.sanitizeEmail("test@example.com"))
        assertEquals("", InputSanitizer.sanitizeEmail("invalid-email"))
        assertEquals("", InputSanitizer.sanitizeEmail(""))
        assertEquals("", InputSanitizer.sanitizeEmail(null))
    }

    @Test
    fun `sanitizeName should validate and clean names`() {
        assertEquals("João Silva", InputSanitizer.sanitizeName("João Silva"))
        assertEquals("", InputSanitizer.sanitizeName("João123"))
        assertEquals("", InputSanitizer.sanitizeName(""))
        assertEquals("", InputSanitizer.sanitizeName(null))
    }

    @Test
    fun `sanitizeForFirestore should remove dangerous characters`() {
        val input = "test.#\$/{}"
        val result = InputSanitizer.sanitizeForFirestore(input)
        // Just check that dangerous chars are removed
        assertFalse(result.contains("."))
        assertFalse(result.contains("#"))
        assertFalse(result.contains("\$"))
    }

    @Test
    fun `preventNoSQLInjection should remove injection patterns`() {
        val input = "test\$where\$ne\$gt"
        val result = InputSanitizer.preventNoSQLInjection(input)
        assertFalse(result.contains("\$where"))
        assertFalse(result.contains("\$ne"))
        assertFalse(result.contains("\$gt"))
    }

    @Test
    fun `sanitizeQueryParam should handle null and empty inputs`() {
        assertEquals("", InputSanitizer.sanitizeQueryParam(null))
        assertEquals("", InputSanitizer.sanitizeQueryParam(""))
        assertEquals("", InputSanitizer.sanitizeQueryParam("   "))
    }

    @Test
    fun `sanitizeCoordinates should validate coordinate pairs`() {
        val valid = InputSanitizer.sanitizeCoordinates(-23.5505, -46.6333)
        assertNotNull(valid)
        assertEquals(-23.5505, valid!!.first, 0.0001)
        assertEquals(-46.6333, valid.second, 0.0001)
        
        assertNull(InputSanitizer.sanitizeCoordinates(null, null))
        assertNull(InputSanitizer.sanitizeCoordinates(91.0, 0.0))
        assertNull(InputSanitizer.sanitizeCoordinates(0.0, 181.0))
    }
}