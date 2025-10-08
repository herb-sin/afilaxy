package com.afilaxy.security

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

object AuthGuard {
    
    private val auth = FirebaseAuth.getInstance()
    
    fun getCurrentUser(): FirebaseUser? = auth.currentUser
    
    fun isUserAuthenticated(): Boolean {
        return try {
            auth.currentUser != null
        } catch (e: Exception) {
            SecurityUtils.safeLog("AuthGuard", "Authentication check failed: ${e.message}", SecurityUtils.LogLevel.ERROR)
            false
        }
    }
    
    fun isEmailVerified(): Boolean = auth.currentUser?.isEmailVerified == true
    
    fun requireAuthentication(): FirebaseUser {
        val user = auth.currentUser
        if (user == null || user.isAnonymous) {
            SecurityUtils.safeLog("AuthGuard", "Authentication required but user not authenticated", SecurityUtils.LogLevel.WARN)
            throw SecurityException("User not authenticated")
        }
        return user
    }
    
    fun requireVerifiedUser(): FirebaseUser {
        val user = requireAuthentication()
        if (!user.isEmailVerified) {
            throw SecurityException("Email not verified")
        }
        return user
    }
    
    fun getCurrentUserId(): String? = auth.currentUser?.uid
    
    fun validateUserAccess(targetUserId: String): Boolean {
        return try {
            if (targetUserId.isBlank()) return false
            val sanitizedTargetId = SecurityValidator.sanitizeInput(targetUserId)
            if (sanitizedTargetId.isEmpty()) return false
            
            val currentUserId = getCurrentUserId()
            currentUserId != null && currentUserId == sanitizedTargetId
        } catch (e: Exception) {
            SecurityUtils.safeLog("AuthGuard", "User access validation failed", SecurityUtils.LogLevel.ERROR)
            false
        }
    }
    
    fun requireUserId(): String {
        return auth.currentUser?.uid ?: throw SecurityException("User ID not available")
    }
    
    inline fun <T> withAuth(action: (FirebaseUser) -> T): T? {
        return try {
            val user = requireAuthentication()
            action(user)
        } catch (e: SecurityException) {
            SecurityUtils.safeLog("AuthGuard", "Authentication required for operation", SecurityUtils.LogLevel.WARN)
            null
        } catch (e: Exception) {
            SecurityUtils.safeLog("AuthGuard", "Unexpected error in authenticated operation: ${e.message}", SecurityUtils.LogLevel.ERROR)
            null
        }
    }
    
    inline fun <T> withVerifiedAuth(action: (FirebaseUser) -> T): T? {
        return try {
            val user = requireVerifiedUser()
            action(user)
        } catch (e: SecurityException) {
            SecurityUtils.safeLog("AuthGuard", "Email verification required for operation", SecurityUtils.LogLevel.WARN)
            null
        } catch (e: Exception) {
            SecurityUtils.safeLog("AuthGuard", "Unexpected error in verified operation: ${e.message}", SecurityUtils.LogLevel.ERROR)
            null
        }
    }
    
    // Add method for emergency operations
    fun requireVerifiedEmail(): FirebaseUser {
        val user = requireAuthentication()
        if (!user.isEmailVerified) {
            SecurityUtils.safeLog("AuthGuard", "Email verification required for emergency operation", SecurityUtils.LogLevel.WARN)
            throw SecurityException("Email verification required")
        }
        return user
    }
}