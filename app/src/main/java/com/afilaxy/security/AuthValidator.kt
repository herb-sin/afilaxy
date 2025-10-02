package com.afilaxy.security

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

object AuthValidator {
    
    fun requireAuthentication(): FirebaseUser {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            throw SecurityException("Operação requer autenticação")
        }
        return user
    }
    
    fun requireVerifiedEmail(): FirebaseUser {
        val user = requireAuthentication()
        if (!user.isEmailVerified) {
            throw SecurityException("Email deve estar verificado")
        }
        return user
    }
    
    fun isUserAuthenticated(): Boolean {
        return FirebaseAuth.getInstance().currentUser != null
    }
    
    fun isEmailVerified(): Boolean {
        val user = FirebaseAuth.getInstance().currentUser
        return user?.isEmailVerified == true
    }
}