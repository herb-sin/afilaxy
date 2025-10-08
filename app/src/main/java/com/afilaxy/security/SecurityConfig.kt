package com.afilaxy.security

object SecurityConfig {
    
    // Security constants
    const val MAX_INPUT_LENGTH = 200
    const val MAX_USER_ID_LENGTH = 128
    const val MAX_COORDINATE_PRECISION = 6
    
    // Rate limiting
    const val MAX_REQUESTS_PER_MINUTE = 60
    const val MAX_EMERGENCY_REQUESTS_PER_HOUR = 5
    
    // Validation patterns
    val SAFE_FILENAME_PATTERN = Regex("^[a-zA-Z0-9._-]{1,100}$")
    val SAFE_ID_PATTERN = Regex("^[a-zA-Z0-9_-]{1,128}$")
    
    // Security headers for HTTP requests
    val SECURITY_HEADERS = mapOf(
        "X-Content-Type-Options" to "nosniff",
        "X-Frame-Options" to "DENY",
        "X-XSS-Protection" to "1; mode=block",
        "Strict-Transport-Security" to "max-age=31536000; includeSubDomains"
    )
    
    fun isSecureEnvironment(): Boolean {
        return !android.os.Build.TYPE.equals("eng", ignoreCase = true)
    }
    
    fun validateSecurityContext(): Boolean {
        return AuthGuard.isUserAuthenticated() && isSecureEnvironment()
    }
}