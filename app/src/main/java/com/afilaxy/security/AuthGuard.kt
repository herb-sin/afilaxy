package com.afilaxy.security

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

sealed class AuthResult {
    data class Authenticated(val userId: String, val token: String) : AuthResult()
    object NotAuthenticated : AuthResult()
    data class Error(val message: String) : AuthResult()
}

object AuthGuard {
    
    suspend fun validateAuthentication(): AuthResult {
        return try {
            val user = FirebaseAuth.getInstance().currentUser
                ?: return AuthResult.NotAuthenticated
            
            if (user.isAnonymous || !user.isEmailVerified) {
                return AuthResult.NotAuthenticated
            }
            
            val tokenResult = user.getIdToken(false).await()
            val currentTime = System.currentTimeMillis()
            val expirationTime = tokenResult.expirationTimestamp * 1000
            
            if (currentTime >= expirationTime) {
                return AuthResult.NotAuthenticated
            }
            
            AuthResult.Authenticated(user.uid, tokenResult.token ?: "")
        } catch (e: Exception) {
            SecureLogger.security("AuthGuard", "Authentication validation failed: ${e.javaClass.simpleName}")
            AuthResult.Error("Authentication failed")
        }
    }
    
    suspend fun requireAuthentication(operation: String): AuthResult {
        val result = validateAuthentication()
        if (result !is AuthResult.Authenticated) {
            SecureLogger.security("AuthGuard", "Unauthorized access attempt: $operation")
        }
        return result
    }
    
    fun getCurrentUserId(): String? {
        return try {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null && !user.isAnonymous && user.isEmailVerified) {
                user.uid
            } else null
        } catch (e: Exception) {
            SecureLogger.security("AuthGuard", "Failed to get user ID: ${e.javaClass.simpleName}")
            null
        }
    }
    
    // Compatibility method for legacy code
    fun isUserAuthenticated(): Boolean {
        return getCurrentUserId() != null
    }
}