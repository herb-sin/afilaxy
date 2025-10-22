package com.afilaxy.security

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

object AuthGuard {
    
    private val auth = FirebaseAuth.getInstance()
    
    fun getCurrentUser(): FirebaseUser? = auth.currentUser
    
    fun isUserAuthenticated(): Boolean {
        return try {
            val user = auth.currentUser
            user != null && !user.isAnonymous && user.uid.isNotBlank()
        } catch (e: Exception) {
            android.util.Log.e("AuthGuard", "Authentication check failed", e)
            false
        }
    }
    
    fun isEmailVerified(): Boolean = auth.currentUser?.isEmailVerified == true
    
    fun requireAuthentication(): FirebaseUser {
        val user = auth.currentUser
        if (user == null || user.isAnonymous) {
            android.util.Log.w("AuthGuard", "Authentication required but user not authenticated")
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
            if (targetUserId.isBlank() || targetUserId.length > 128) return false
            
            // Validate UID format (Firebase UIDs are alphanumeric)
            if (!targetUserId.matches(Regex("^[a-zA-Z0-9]{28}$"))) return false
            
            val currentUserId = getCurrentUserId()
            currentUserId != null && currentUserId == targetUserId
        } catch (e: Exception) {
            android.util.Log.e("AuthGuard", "User access validation failed", e)
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
            android.util.Log.w("AuthGuard", "Authentication required for operation")
            null
        } catch (e: Exception) {
            android.util.Log.e("AuthGuard", "Unexpected error in authenticated operation", e)
            null
        }
    }
    
    inline fun <T> withVerifiedAuth(action: (FirebaseUser) -> T): T? {
        return try {
            val user = requireVerifiedUser()
            action(user)
        } catch (e: SecurityException) {
            android.util.Log.w("AuthGuard", "Email verification required for operation")
            null
        } catch (e: Exception) {
            android.util.Log.e("AuthGuard", "Unexpected error in verified operation", e)
            null
        }
    }
    
    // Add method for emergency operations
    fun requireVerifiedEmail(): FirebaseUser {
        val user = requireAuthentication()
        if (!user.isEmailVerified) {
            android.util.Log.w("AuthGuard", "Email verification required for emergency operation")
            throw SecurityException("Email verification required")
        }
        return user
    }
}