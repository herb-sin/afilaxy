package com.afilaxy.security

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

object AuthValidator {
    
    fun requireAuthentication(): FirebaseUser {
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        
        if (user == null || user.uid.isBlank()) {
            throw SecurityException("Operação requer autenticação válida")
        }
        
        // Verificar se o token ainda é válido
        user.getIdToken(false).addOnFailureListener {
            throw SecurityException("Token de autenticação inválido")
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
        val user = FirebaseAuth.getInstance().currentUser
        return user != null && user.uid.isNotBlank()
    }
    
    fun getCurrentUserId(): String? {
        return try {
            requireAuthentication().uid
        } catch (e: SecurityException) {
            null
        }
    }
    
    fun validateUserAccess(targetUserId: String): Boolean {
        val currentUserId = getCurrentUserId()
        return currentUserId != null && currentUserId == targetUserId
    }
    
    fun isEmailVerified(): Boolean {
        val user = FirebaseAuth.getInstance().currentUser
        return user?.isEmailVerified == true
    }
}