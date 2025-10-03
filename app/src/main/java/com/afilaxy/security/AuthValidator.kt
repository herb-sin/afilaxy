package com.afilaxy.security

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

object AuthValidator {
    
    private val auth by lazy { FirebaseAuth.getInstance() }
    
    fun requireAuthentication(): FirebaseUser {
        val user = auth.currentUser
        
        if (user == null || user.uid.isBlank()) {
            throw SecurityException("Operação requer autenticação válida")
        }
        
        // Verificar se o token ainda é válido (sem bloquear)
        try {
            user.getIdToken(false)
        } catch (e: Exception) {
            android.util.Log.w("AuthValidator", "Aviso: Token pode estar expirado")
            // Não lançar exceção aqui para não bloquear operações
        }
        
        return user
    }
    
    fun requireVerifiedEmail(): FirebaseUser {
        return try {
            val user = requireAuthentication()
            if (!user.isEmailVerified) {
                throw SecurityException("Email deve estar verificado")
            }
            user
        } catch (e: Exception) {
            android.util.Log.e("AuthValidator", "Falha na verificação de email")
            throw e
        }
    }
    
    fun isUserAuthenticated(): Boolean {
        val user = auth.currentUser
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
        val user = auth.currentUser
        return user?.isEmailVerified == true
    }
}