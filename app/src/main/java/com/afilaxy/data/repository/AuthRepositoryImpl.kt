package com.afilaxy.data.repository

import com.afilaxy.domain.repository.IAuthRepository
import com.afilaxy.security.AuthGuard
import com.afilaxy.security.AuthResult
import com.google.firebase.auth.FirebaseAuth

class AuthRepositoryImpl : IAuthRepository {
    private val auth = FirebaseAuth.getInstance()
    
    override suspend fun validateAuthentication(): AuthResult {
        return AuthGuard.validateAuthentication()
    }
    
    override suspend fun signOut(): Boolean {
        return try {
            auth.signOut()
            true
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Sign out failed: ${e.javaClass.simpleName}")
            false
        }
    }
    
    override fun getCurrentUserId(): String? {
        return AuthGuard.getCurrentUserId()
    }
    
    override fun isLoggedIn(): Boolean {
        return auth.currentUser != null
    }
}