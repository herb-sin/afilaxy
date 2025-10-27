package com.afilaxy.security

object SecurityInterceptor {
    fun validateOperation(operation: String): Boolean {
        return AuthGuard.isUserAuthenticated() && 
               !operation.contains("DROP") && 
               !operation.contains("DELETE")
    }
    
    fun intercept(operation: String, block: () -> Unit) {
        if (validateOperation(operation)) {
            block()
        } else {
            SecureLogger.security("SECURITY_INTERCEPTOR", "OPERATION_BLOCKED: $operation")
        }
    }
}