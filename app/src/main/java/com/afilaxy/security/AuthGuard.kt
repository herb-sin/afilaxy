package com.afilaxy.security

import com.google.firebase.auth.FirebaseAuth

/**
 * Authentication guard for Afilaxy application
 * Provides centralized authentication checks and user session management
 */
object AuthGuard {
    
    private val firebaseAuth by lazy { FirebaseAuth.getInstance() }
    
    /**
     * Check if user is currently authenticated
     */
    fun isUserAuthenticated(): Boolean {
        return try {
            val currentUser = firebaseAuth.currentUser
            currentUser != null && !currentUser.isAnonymous
        } catch (e: Exception) {
            SecureLogger.e("AuthGuard", "Error checking authentication", e)
            false
        }
    }
    
    /**
     * Get current user ID safely
     */
    fun getCurrentUserId(): String? {
        return try {
            firebaseAuth.currentUser?.uid
        } catch (e: Exception) {
            SecureLogger.e("AuthGuard", "Error getting user ID", e)
            null
        }
    }
    
    /**
     * Get current user email safely
     */
    fun getCurrentUserEmail(): String? {
        return try {
            val email = firebaseAuth.currentUser?.email
            if (email != null && InputSanitizer.isValidEmail(email)) {
                email
            } else {
                null
            }
        } catch (e: Exception) {
            SecureLogger.e("AuthGuard", "Error getting user email", e)
            null
        }
    }
    
    /**
     * Require authentication for sensitive operations
     */
    fun requireAuthentication(operation: String): Boolean {
        return if (isUserAuthenticated()) {
            true
        } else {
            SecureLogger.security("AUTH_REQUIRED", "OPERATION_BLOCKED", operation)
            false
        }
    }
    
    /**
     * Require authentication and return user
     */
    fun requireAuthentication(): com.google.firebase.auth.FirebaseUser {
        val user = firebaseAuth.currentUser
        if (user == null || user.isAnonymous) {
            throw SecurityException("Authentication required")
        }
        return user
    }
    
    /**
     * Check if user has been authenticated recently
     */
    fun isRecentlyAuthenticated(maxAgeMinutes: Long = 30): Boolean {
        return try {
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) return false
            
            val metadata = currentUser.metadata
            val lastSignIn = metadata?.lastSignInTimestamp ?: 0
            val currentTime = System.currentTimeMillis()
            val maxAge = maxAgeMinutes * 60 * 1000 // Convert to milliseconds
            
            (currentTime - lastSignIn) <= maxAge
        } catch (e: Exception) {
            SecureLogger.e("AuthGuard", "Error checking recent authentication", e)
            false
        }
    }
    
    /**
     * Sign out user safely
     */
    fun signOut() {
        try {
            val userId = getCurrentUserId()
            firebaseAuth.signOut()
            SecureLogger.security("USER_SIGNOUT", "SUCCESS", userId)
        } catch (e: Exception) {
            SecureLogger.e("AuthGuard", "Error during sign out", e)
        }
    }
}