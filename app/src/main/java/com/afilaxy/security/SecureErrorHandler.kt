package com.afilaxy.security

import android.util.Log

object SecureErrorHandler {
    
    private const val MAX_ERROR_MESSAGE_LENGTH = 100
    
    /**
     * Handles errors securely without exposing sensitive information
     */
    fun handleError(
        tag: String,
        operation: String,
        error: Throwable,
        userMessage: String = "Ocorreu um erro. Tente novamente."
    ): String {
        // Log technical details securely
        val sanitizedTag = SecurityUtils.sanitizeLogInput(tag)
        val sanitizedOperation = SecurityUtils.sanitizeLogInput(operation)
        
        // Don't log full stack traces in production
        val errorType = error.javaClass.simpleName
        val sanitizedMessage = error.message?.take(50)?.let { 
            SecurityUtils.sanitizeLogInput(it) 
        } ?: "Unknown error"
        
        Log.e(sanitizedTag, "Operation: $sanitizedOperation, Error: $errorType - $sanitizedMessage")
        
        // Return safe user message
        return userMessage.take(MAX_ERROR_MESSAGE_LENGTH)
    }
    
    /**
     * Handles security-related errors with appropriate logging
     */
    fun handleSecurityError(
        tag: String,
        operation: String,
        error: SecurityException,
        logLevel: SecurityUtils.LogLevel = SecurityUtils.LogLevel.WARN
    ): String {
        SecurityUtils.safeLog(
            tag,
            "Security violation in operation: ${SecurityUtils.sanitizeLogInput(operation)}",
            logLevel
        )
        
        return "Acesso negado. Verifique suas permissões."
    }
    
    /**
     * Handles authentication errors
     */
    fun handleAuthError(
        tag: String,
        operation: String
    ): String {
        SecurityUtils.safeLog(
            tag,
            "Authentication required for operation: ${SecurityUtils.sanitizeLogInput(operation)}",
            SecurityUtils.LogLevel.WARN
        )
        
        return "É necessário fazer login para continuar."
    }
    
    /**
     * Handles network errors safely
     */
    fun handleNetworkError(
        tag: String,
        error: Throwable
    ): String {
        val errorType = when {
            error.message?.contains("timeout", ignoreCase = true) == true -> "timeout"
            error.message?.contains("network", ignoreCase = true) == true -> "network"
            error.message?.contains("connection", ignoreCase = true) == true -> "connection"
            else -> "network"
        }
        
        SecurityUtils.safeLog(
            tag,
            "Network error type: $errorType",
            SecurityUtils.LogLevel.WARN
        )
        
        return when (errorType) {
            "timeout" -> "Tempo limite excedido. Verifique sua conexão."
            "connection" -> "Problema de conexão. Tente novamente."
            else -> "Erro de rede. Verifique sua conexão com a internet."
        }
    }
    
    /**
     * Handles validation errors
     */
    fun handleValidationError(
        field: String,
        reason: String = "formato inválido"
    ): String {
        val sanitizedField = SecurityUtils.sanitizeLogInput(field)
        val sanitizedReason = SecurityUtils.sanitizeLogInput(reason)
        
        SecurityUtils.safeLog(
            "Validation",
            "Field validation failed: $sanitizedField - $sanitizedReason",
            SecurityUtils.LogLevel.DEBUG
        )
        
        return "Verifique o campo $sanitizedField: $sanitizedReason."
    }
}