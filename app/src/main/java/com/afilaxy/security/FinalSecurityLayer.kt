package com.afilaxy.security

object FinalSecurityLayer {
    
    fun validateAllInputs(vararg inputs: String?): Boolean {
        return inputs.all { input ->
            input?.let { InputSanitizer.sanitizeText(it) == it } ?: true
        }
    }
    
    fun secureOperation(operationName: String, block: () -> Unit): Boolean {
        return if (SecurityUtils.validateOperation(operationName)) {
            try {
                block()
                true
            } catch (e: Exception) {
                SecurityUtils.safeLog("FinalSecurityLayer", "Operation failed: $operationName", SecurityUtils.LogLevel.ERROR)
                false
            }
        } else {
            false
        }
    }
    
    fun isSecureContext(): Boolean {
        return AuthGuard.isUserAuthenticated() && SecurityConfig.validateSecurityContext()
    }
}