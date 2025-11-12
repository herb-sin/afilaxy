package com.afilaxy.domain.repository

import com.afilaxy.security.AuthResult

interface IAuthRepository {
    suspend fun validateAuthentication(): AuthResult
    suspend fun signOut(): Boolean
    fun getCurrentUserId(): String?
    fun isLoggedIn(): Boolean
}