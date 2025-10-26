package com.afilaxy.security

import kotlinx.coroutines.runBlocking

object SecurityInterceptor {
    
    private val rateLimiter = mutableMapOf<String, Long>()
    private const val RATE_LIMIT_MS = 1000L
    
    fun <T> secureOperation(
        operationId: String,
        requireAuth: Boolean = true,
        block: () -> T
    ): T? {
        return try {
            if (requireAuth && !AuthGuard.isUserAuthenticated()) {
                SecureLogger.security("SECURITY_INTERCEPTOR", "UNAUTHENTICATED_ACCESS: $operationId")
                return null
            }
            
            if (!checkRateLimit(operationId)) {
                SecureLogger.security("SECURITY_INTERCEPTOR", "RATE_LIMIT_EXCEEDED: $operationId")
                return null
            }
            
            block()
        } catch (e: SecurityException) {
            SecureLogger.security("SECURITY_INTERCEPTOR", "SECURITY_VIOLATION: $operationId")
            null
        } catch (e: Exception) {
            SecureLogger.e("SecurityInterceptor", "Operation failed: $operationId", e)
            null
        }
    }
    
    suspend fun <T> secureAsyncOperation(
        operationId: String,
        requireAuth: Boolean = true,
        block: suspend () -> T
    ): T? {
        return try {
            if (requireAuth && !AuthGuard.isUserAuthenticated()) {
                SecureLogger.security("SECURITY_INTERCEPTOR", "UNAUTHENTICATED_ASYNC_ACCESS: $operationId")
                return null
            }
            
            if (!checkRateLimit(operationId)) {
                SecureLogger.security("SECURITY_INTERCEPTOR", "ASYNC_RATE_LIMIT_EXCEEDED: $operationId")
                return null
            }
            
            block()
        } catch (e: SecurityException) {
            SecureLogger.security("SECURITY_INTERCEPTOR", "ASYNC_SECURITY_VIOLATION: $operationId")
            null
        } catch (e: Exception) {
            SecureLogger.e("SecurityInterceptor", "Async operation failed: $operationId", e)
            null
        }
    }
    
    private fun checkRateLimit(operationId: String): Boolean {
        val now = System.currentTimeMillis()
        val lastCall = rateLimiter[operationId] ?: 0
        
        return if (now - lastCall > RATE_LIMIT_MS) {
            rateLimiter[operationId] = now
            true
        } else false
    }
}