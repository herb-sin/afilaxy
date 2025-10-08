package com.afilaxy.security

object SecureValidator {
    
    fun validateAndSanitizeInput(input: String?, maxLength: Int = 100): String {
        if (input.isNullOrBlank()) return ""
        
        if (!AuthGuard.isUserAuthenticated()) {
            SecurityUtils.safeLog("SecureValidator", "Validation denied - authentication required", SecurityUtils.LogLevel.WARN)
            return ""
        }
        
        return InputSanitizer.sanitizeText(input).take(maxLength)
    }
    
    fun validateCoordinates(lat: Double, lon: Double): Boolean {
        return lat in -90.0..90.0 && lon in -180.0..180.0
    }
    
    fun validateUserId(userId: String?): Boolean {
        if (userId.isNullOrBlank()) return false
        return userId.matches(Regex("^[a-zA-Z0-9_-]{1,128}$"))
    }
    
    fun validateEmail(email: String?): Boolean {
        return InputSanitizer.isValidEmail(email)
    }
    
    fun requireAuthentication(operation: String): Boolean {
        return SecurityUtils.requireAuthentication(operation)
    }
}