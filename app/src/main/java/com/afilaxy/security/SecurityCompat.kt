package com.afilaxy.security

/**
 * Compatibility layer to ensure security features don't break existing functionality
 */
object SecurityCompat {
    
    // Safe wrapper for operations that might not have security context
    inline fun <T> safeExecute(
        operation: String,
        requireAuth: Boolean = false,
        fallback: T,
        action: () -> T
    ): T {
        return try {
            if (requireAuth && !AuthGuard.isUserAuthenticated()) {
                SecurityUtils.safeLog("SecurityCompat", "Operation $operation requires authentication", SecurityUtils.LogLevel.WARN)
                return fallback
            }
            
            action()
        } catch (e: SecurityException) {
            SecurityUtils.safeLog("SecurityCompat", "Security exception in $operation: ${e.message}", SecurityUtils.LogLevel.WARN)
            fallback
        } catch (e: Exception) {
            SecurityUtils.safeLog("SecurityCompat", "Unexpected error in $operation: ${e.message}", SecurityUtils.LogLevel.ERROR)
            fallback
        }
    }
    
    // Gradual security enforcement - warn but don't block
    fun enforceGradually(operation: String, condition: () -> Boolean): Boolean {
        return try {
            val result = condition()
            if (!result) {
                SecurityUtils.safeLog("SecurityCompat", "Security condition failed for $operation (warning only)", SecurityUtils.LogLevel.WARN)
            }
            true // Always return true for gradual enforcement
        } catch (e: Exception) {
            SecurityUtils.safeLog("SecurityCompat", "Security check error for $operation: ${e.message}", SecurityUtils.LogLevel.ERROR)
            true // Don't block on errors during transition
        }
    }
    
    // Check if we're in development mode
    fun isDevelopmentMode(): Boolean {
        return try {
            android.os.Build.FINGERPRINT.contains("generic") ||
            android.os.Build.MODEL.contains("Emulator") ||
            android.os.Build.MANUFACTURER.contains("Genymotion") ||
            com.afilaxy.BuildConfig.DEBUG
        } catch (e: Exception) {
            false
        }
    }
}