package com.afilaxy.security

import com.google.firebase.auth.FirebaseAuth

object AuthGuard {
    
    fun isUserAuthenticated(): Boolean {
        return try {
            val user = FirebaseAuth.getInstance().currentUser
            user != null && !user.isAnonymous && user.isEmailVerified
        } catch (e: Exception) {
            SecureLogger.e("AuthGuard", "Authentication check failed", e)
            false
        }
    }
    
    fun requireAuthentication(operation: String): Boolean {
        return if (isUserAuthenticated()) {
            true
        } else {
            SecureLogger.security("AUTH_GUARD", "UNAUTHENTICATED_ACCESS: $operation")
            false
        }
    }
    
    fun getCurrentUserId(): String? {
        return try {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null && !user.isAnonymous) user.uid else null
        } catch (e: Exception) {
            SecureLogger.e("AuthGuard", "Failed to get user ID", e)
            null
        }
    }
    
    fun validateUserSession(): Boolean {
        return try {
            val user = FirebaseAuth.getInstance().currentUser
            user?.reload()
            isUserAuthenticated()
        } catch (e: Exception) {
            SecureLogger.e("AuthGuard", "Session validation failed", e)
            false
        }
    }
    
    fun isAdmin(): Boolean {
        return try {
            val user = FirebaseAuth.getInstance().currentUser
            user?.getIdToken(false)?.result?.claims?.get("admin") == true
        } catch (e: Exception) {
            SecureLogger.e("AuthGuard", "Admin check failed", e)
            false
        }
    }
    
    fun isSessionValid(): Boolean {
        return try {
            val user = FirebaseAuth.getInstance().currentUser
            user?.let {
                val tokenResult = it.getIdToken(false).result
                val expirationTime = tokenResult.expirationTimestamp * 1000
                System.currentTimeMillis() < expirationTime
            } ?: false
        } catch (e: Exception) {
            SecureLogger.e("AuthGuard", "Session validation failed", e)
            false
        }
    }
}