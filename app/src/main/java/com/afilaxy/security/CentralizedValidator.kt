package com.afilaxy.security

import java.util.regex.Pattern

object CentralizedValidator {
    
    private val SAFE_INPUT_PATTERN = Pattern.compile("^[a-zA-Z0-9\\s._@-]{1,200}$")
    private val EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]{1,64}@[a-zA-Z0-9.-]{1,253}\\.[a-zA-Z]{2,6}$")
    private val COORDINATE_RANGE = -180.0..180.0
    
    private val BLOCKED_PATTERNS = setOf(
        "javascript:", "eval(", "function(", "<script", "<?xml", "<!DOCTYPE",
        "$where", "$ne", "$gt", "$lt", "$regex", "$or", "$and", "ObjectId(",
        "../", "..\\", "null", "undefined", "constructor", "prototype"
    )
    
    fun validateInput(input: String?, type: InputType): ValidationResult {
        if (input.isNullOrBlank()) return ValidationResult.invalid("Empty input")
        
        if (input.length > 1000) return ValidationResult.invalid("Input too long")
        
        if (BLOCKED_PATTERNS.any { input.contains(it, ignoreCase = true) }) {
            SecureLogger.security("VALIDATION", "BLOCKED_PATTERN_DETECTED")
            return ValidationResult.invalid("Dangerous pattern detected")
        }
        
        return when (type) {
            InputType.GENERAL -> if (SAFE_INPUT_PATTERN.matcher(input).matches()) 
                ValidationResult.valid() else ValidationResult.invalid("Invalid characters")
            InputType.EMAIL -> if (EMAIL_PATTERN.matcher(input).matches()) 
                ValidationResult.valid() else ValidationResult.invalid("Invalid email")
            InputType.COORDINATE -> validateCoordinate(input)
            InputType.FILE_PATH -> SecurityValidator.validateFilePath(input).let {
                if (it) ValidationResult.valid() else ValidationResult.invalid("Invalid file path")
            }
        }
    }
    
    private fun validateCoordinate(input: String): ValidationResult {
        return try {
            val coord = input.toDouble()
            if (coord in COORDINATE_RANGE && !coord.isNaN() && !coord.isInfinite()) 
                ValidationResult.valid() else ValidationResult.invalid("Invalid coordinate")
        } catch (e: NumberFormatException) {
            ValidationResult.invalid("Invalid number format")
        }
    }
    
    enum class InputType { GENERAL, EMAIL, COORDINATE, FILE_PATH }
}

data class ValidationResult(val isValid: Boolean, val error: String?) {
    companion object {
        fun valid() = ValidationResult(true, null)
        fun invalid(error: String) = ValidationResult(false, error)
    }
}