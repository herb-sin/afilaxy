package com.afilaxy.security

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

object AuthGuard {
    
    private val auth = FirebaseAuth.getInstance()
    
    fun getCurrentUser(): FirebaseUser? = auth.currentUser
    
    fun isAuthenticated(): Boolean = auth.currentUser != null
    
    fun isEmailVerified(): Boolean = auth.currentUser?.isEmailVerified == true
    
    fun requireAuthentication(): FirebaseUser {
        return auth.currentUser ?: throw SecurityException("User not authenticated")
    }
    
    fun requireVerifiedUser(): FirebaseUser {
        val user = requireAuthentication()
        if (!user.isEmailVerified) {
            throw SecurityException("Email not verified")
        }
        return user
    }
    
    fun requireVerifiedEmail(): FirebaseUser = requireVerifiedUser()
    
    fun getCurrentUserId(): String? = auth.currentUser?.uid
    
    fun validateUserAccess(targetUserId: String): Boolean {
        val currentUserId = getCurrentUserId()
        return currentUserId != null && currentUserId == targetUserId
    }
    
    fun isUserAuthenticated(): Boolean = isAuthenticated()
    
    fun getUserId(): String? = auth.currentUser?.uid
    
    fun requireUserId(): String {
        return auth.currentUser?.uid ?: throw SecurityException("User ID not available")
    }
    
    inline fun <T> withAuth(action: (FirebaseUser) -> T): T? {
        return try {
            val user = requireAuthentication()
            action(user)
        } catch (e: SecurityException) {
            null
        }
    }
    
    inline fun <T> withVerifiedAuth(action: (FirebaseUser) -> T): T? {
        return try {
            val user = requireVerifiedUser()
            action(user)
        } catch (e: SecurityException) {
            null
        }
    }
}