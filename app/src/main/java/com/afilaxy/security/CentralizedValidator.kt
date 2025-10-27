package com.afilaxy.security

import java.util.regex.Pattern

/**
 * Centralized validation system for all input types
 * Provides comprehensive validation with security-first approach
 */
object CentralizedValidator {
    
    private val SAFE_INPUT_PATTERN = Pattern.compile("^[a-zA-Z0-9\\s._@-]{1,200}$")
    private val EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]{1,64}@[a-zA-Z0-9.-]{1,253}\\.[a-zA-Z]{2,6}$")
    private val COORDINATE_RANGE = -180.0..180.0
    
    private val BLOCKED_PATTERNS = setOf(
        "javascript:", "eval(", "function(", "<script", "<?xml", "<!DOCTYPE",
        "\$where", "\$ne", "\$gt", "\$lt", "\$regex", "\$or", "\$and", "ObjectId(",
        "../", "..\\", "null", "undefined", "constructor", "prototype"
    )
    
    fun validateInput(input: String?, type: InputType): ValidationResult {
        if (input.isNullOrBlank()) return ValidationResult.Invalid(listOf("Empty input"))
        
        if (input.length > 1000) return ValidationResult.Invalid(listOf("Input too long"))
        
        if (BLOCKED_PATTERNS.any { input.contains(it, ignoreCase = true) }) {
            SecureLogger.security("VALIDATION", "BLOCKED_PATTERN_DETECTED")
            return ValidationResult.Invalid(listOf("Dangerous pattern detected"))
        }
        
        return when (type) {
            InputType.GENERAL -> if (SAFE_INPUT_PATTERN.matcher(input).matches()) 
                ValidationResult.Valid else ValidationResult.Invalid(listOf("Invalid characters"))
            InputType.EMAIL -> if (EMAIL_PATTERN.matcher(input).matches()) 
                ValidationResult.Valid else ValidationResult.Invalid(listOf("Invalid email"))
            InputType.COORDINATE -> validateCoordinate(input)
            InputType.FILE_PATH -> if (isValidFilePath(input))
                ValidationResult.Valid else ValidationResult.Invalid(listOf("Invalid file path"))
        }
    }
    
    private fun validateCoordinate(input: String): ValidationResult {
        return try {
            val coord = input.toDouble()
            if (coord in COORDINATE_RANGE && !coord.isNaN() && !coord.isInfinite()) 
                ValidationResult.Valid else ValidationResult.Invalid(listOf("Invalid coordinate"))
        } catch (e: NumberFormatException) {
            ValidationResult.Invalid(listOf("Invalid number format"))
        }
    }
    
    private fun isValidFilePath(path: String): Boolean {
        val allowedExtensions = setOf(".jpg", ".jpeg", ".png", ".pdf", ".txt")
        return allowedExtensions.any { path.lowercase().endsWith(it) } && 
               !path.contains("..") && 
               path.length < 255
    }
    
    enum class InputType { GENERAL, EMAIL, COORDINATE, FILE_PATH }
}