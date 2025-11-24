package com.afilaxy.security

interface AuthProvider {
    fun isUserAuthenticated(): Boolean
    fun getCurrentUserId(): String?
}

class FirebaseAuthProvider : AuthProvider {
    override fun isUserAuthenticated(): Boolean = AuthGuard.isUserAuthenticated()
    override fun getCurrentUserId(): String? = AuthGuard.getCurrentUserId()
}
