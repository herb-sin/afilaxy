package com.afilaxy.security

import android.util.Log
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlin.coroutines.CoroutineContext

/**
 * Centralized error handling system for Afilaxy
 * Provides secure error logging and user-friendly error messages
 */
object ErrorHandler {
    
    private const val TAG = "AfilaxyErrorHandler"
    
    /**
     * Global coroutine exception handler for unhandled exceptions
     */
    val globalExceptionHandler = CoroutineExceptionHandler { context, exception ->
        handleException(exception, "GlobalCoroutine", context.toString())
    }
    
    /**
     * Handle exceptions with secure logging and user feedback
     */
    fun handleException(
        exception: Throwable,
        operation: String,
        context: String = "",
        showToUser: Boolean = false
    ): AfilaxyError {
        val sanitizedOperation = InputSanitizer.sanitizeText(operation)
        val sanitizedContext = InputSanitizer.sanitizeText(context)
        
        val afilaxyError = when (exception) {
            is SecurityException -> AfilaxyError.Security(
                message = "Operação não autorizada",
                securityOperation = sanitizedOperation,
                cause = exception
            )
            is IllegalArgumentException -> AfilaxyError.Validation(
                message = "Dados inválidos fornecidos",
                validationOperation = sanitizedOperation,
                cause = exception
            )
            is java.net.UnknownHostException -> AfilaxyError.Network(
                message = "Sem conexão com a internet",
                networkOperation = sanitizedOperation,
                cause = exception
            )
            is java.net.SocketTimeoutException -> AfilaxyError.Network(
                message = "Tempo limite de conexão excedido",
                networkOperation = sanitizedOperation,
                cause = exception
            )
            is com.google.firebase.FirebaseException -> AfilaxyError.Firebase(
                message = "Erro no serviço Firebase",
                firebaseOperation = sanitizedOperation,
                cause = exception
            )
            else -> AfilaxyError.Unknown(
                message = "Erro inesperado",
                unknownOperation = sanitizedOperation,
                cause = exception
            )
        }
        
        // Secure logging - no sensitive data
        SecurityUtils.safeLog(
            TAG,
            "Error in $sanitizedOperation: ${afilaxyError.javaClass.simpleName}",
            SecurityUtils.LogLevel.ERROR
        )
        
        // Detailed logging for debugging (only in debug builds)
        try {
            if (com.afilaxy.BuildConfig.DEBUG) {
                Log.e(TAG, "Debug details for $sanitizedOperation", exception)
            }
        } catch (e: Exception) {
            // Ignore if BuildConfig not available
        }
        
        return afilaxyError
    }
    
    /**
     * Handle network errors specifically
     */
    fun handleNetworkError(exception: Throwable, operation: String): AfilaxyError.Network {
        return when (exception) {
            is java.net.UnknownHostException -> AfilaxyError.Network(
                message = "Verifique sua conexão com a internet",
                networkOperation = operation,
                cause = exception
            )
            is java.net.SocketTimeoutException -> AfilaxyError.Network(
                message = "Conexão muito lenta. Tente novamente",
                networkOperation = operation,
                cause = exception
            )
            else -> AfilaxyError.Network(
                message = "Erro de conexão",
                networkOperation = operation,
                cause = exception
            )
        }
    }
    
    /**
     * Handle Firebase-specific errors
     */
    fun handleFirebaseError(exception: Throwable, operation: String): AfilaxyError.Firebase {
        val message = when {
            exception.message?.contains("permission-denied") == true -> 
                "Permissão negada para esta operação"
            exception.message?.contains("network-request-failed") == true -> 
                "Falha na conexão. Verifique sua internet"
            exception.message?.contains("too-many-requests") == true -> 
                "Muitas tentativas. Aguarde um momento"
            else -> "Erro no serviço. Tente novamente"
        }
        
        return AfilaxyError.Firebase(
            message = message,
            firebaseOperation = operation,
            cause = exception
        )
    }
    
    /**
     * Create user-friendly error messages
     */
    fun getUserMessage(error: AfilaxyError): String {
        return when (error) {
            is AfilaxyError.Security -> "Acesso negado. Faça login novamente"
            is AfilaxyError.Network -> error.message
            is AfilaxyError.Firebase -> error.message
            is AfilaxyError.Validation -> "Verifique os dados inseridos"
            is AfilaxyError.Unknown -> "Erro inesperado. Tente novamente"
        }
    }
}

/**
 * Sealed class representing different types of errors in Afilaxy
 */
sealed class AfilaxyError(
    override val message: String,
    val operation: String,
    override val cause: Throwable?
) : Exception(message, cause) {
    
    data class Security(
        override val message: String,
        val securityOperation: String,
        override val cause: Throwable?
    ) : AfilaxyError(message, securityOperation, cause)
    
    data class Network(
        override val message: String,
        val networkOperation: String,
        override val cause: Throwable?
    ) : AfilaxyError(message, networkOperation, cause)
    
    data class Firebase(
        override val message: String,
        val firebaseOperation: String,
        override val cause: Throwable?
    ) : AfilaxyError(message, firebaseOperation, cause)
    
    data class Validation(
        override val message: String,
        val validationOperation: String,
        override val cause: Throwable?
    ) : AfilaxyError(message, validationOperation, cause)
    
    data class Unknown(
        override val message: String,
        val unknownOperation: String,
        override val cause: Throwable?
    ) : AfilaxyError(message, unknownOperation, cause)
}