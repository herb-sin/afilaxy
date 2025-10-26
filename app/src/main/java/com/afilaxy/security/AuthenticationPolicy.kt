package com.afilaxy.security

import android.content.Context

object AuthenticationPolicy {
    
    private val PROTECTED_OPERATIONS = setOf(
        "emergency_create", "emergency_update", "cache_cleanup", "file_upload",
        "user_data_access", "location_access", "notification_send", "backup_create"
    )
    
    private val ADMIN_OPERATIONS = setOf(
        "system_config", "security_audit", "user_management"
    )
    
    fun requireAuthentication(operation: String): Boolean {
        return when {
            ADMIN_OPERATIONS.contains(operation) -> {
                if (!AuthGuard.isUserAuthenticated() || !AuthGuard.isAdmin()) {
                    SecureLogger.security("AUTH_POLICY", "ADMIN_ACCESS_DENIED: $operation")
                    false
                } else true
            }
            PROTECTED_OPERATIONS.contains(operation) -> {
                if (!AuthGuard.isUserAuthenticated()) {
                    SecureLogger.security("AUTH_POLICY", "AUTH_REQUIRED: $operation")
                    false
                } else true
            }
            else -> true
        }
    }
    
    fun validateSession(): Boolean {
        return try {
            AuthGuard.isUserAuthenticated() && AuthGuard.isSessionValid()
        } catch (e: Exception) {
            SecureLogger.e("AuthenticationPolicy", "Session validation failed", e)
            false
        }
    }
    
    fun enforcePolicy(operation: String, context: Context? = null): Boolean {
        return SecurityInterceptor.secureOperation("auth_policy_$operation") {
            requireAuthentication(operation) && validateSession()
        } ?: false
    }
}