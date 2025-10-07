package com.afilaxy.utils

import com.afilaxy.security.InputSanitizer
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestoreException
import java.net.UnknownHostException
import java.util.concurrent.TimeoutException

object ErrorHandler {
    
    data class ErrorResult(
        val userMessage: String,
        val shouldRetry: Boolean = false,
        val logMessage: String = ""
    )
    
    fun handleError(exception: Throwable, operation: String = ""): ErrorResult {
        val sanitizedMessage = InputSanitizer.sanitizeText(exception.message)
        
        return when (exception) {
            is FirebaseAuthException -> handleAuthError(exception)
            is FirebaseFirestoreException -> handleFirestoreError(exception)
            is UnknownHostException -> ErrorResult(
                userMessage = "Sem conexão com a internet. Verifique sua rede.",
                shouldRetry = true,
                logMessage = "Erro de rede em $operation: $sanitizedMessage"
            )
            is TimeoutException -> ErrorResult(
                userMessage = "Operação demorou muito. Tente novamente.",
                shouldRetry = true,
                logMessage = "Timeout em $operation: $sanitizedMessage"
            )
            is SecurityException -> ErrorResult(
                userMessage = "Erro de autenticação. Faça login novamente.",
                shouldRetry = false,
                logMessage = "Erro de segurança em $operation: $sanitizedMessage"
            )
            else -> ErrorResult(
                userMessage = "Erro inesperado. Tente novamente.",
                shouldRetry = true,
                logMessage = "Erro genérico em $operation: $sanitizedMessage"
            )
        }
    }
    
    private fun handleAuthError(exception: FirebaseAuthException): ErrorResult {
        val userMessage = when (exception.errorCode) {
            "ERROR_NETWORK_REQUEST_FAILED" -> "Sem conexão. Verifique sua internet."
            "ERROR_USER_NOT_FOUND" -> "Usuário não encontrado."
            "ERROR_WRONG_PASSWORD" -> "Senha incorreta."
            "ERROR_EMAIL_ALREADY_IN_USE" -> "Email já cadastrado."
            "ERROR_WEAK_PASSWORD" -> "Senha muito fraca."
            "ERROR_INVALID_EMAIL" -> "Email inválido."
            else -> "Erro de autenticação. Tente novamente."
        }
        
        return ErrorResult(
            userMessage = userMessage,
            shouldRetry = exception.errorCode == "ERROR_NETWORK_REQUEST_FAILED",
            logMessage = "Auth error: ${exception.errorCode}"
        )
    }
    
    private fun handleFirestoreError(exception: FirebaseFirestoreException): ErrorResult {
        val userMessage = when (exception.code) {
            FirebaseFirestoreException.Code.UNAVAILABLE -> "Serviço temporariamente indisponível."
            FirebaseFirestoreException.Code.DEADLINE_EXCEEDED -> "Operação demorou muito. Tente novamente."
            FirebaseFirestoreException.Code.PERMISSION_DENIED -> "Sem permissão para esta operação."
            FirebaseFirestoreException.Code.UNAUTHENTICATED -> "Faça login novamente."
            else -> "Erro no servidor. Tente novamente."
        }
        
        return ErrorResult(
            userMessage = userMessage,
            shouldRetry = exception.code in listOf(
                FirebaseFirestoreException.Code.UNAVAILABLE,
                FirebaseFirestoreException.Code.DEADLINE_EXCEEDED
            ),
            logMessage = "Firestore error: ${exception.code}"
        )
    }
    
    inline fun <T> safeCall(
        operation: String,
        onError: (ErrorResult) -> Unit = {},
        block: () -> T
    ): T? {
        return try {
            block()
        } catch (e: Exception) {
            val errorResult = handleError(e, operation)
            android.util.Log.e("ErrorHandler", errorResult.logMessage)
            onError(errorResult)
            null
        }
    }
    
    suspend inline fun <T> safeSuspendCall(
        operation: String,
        onError: (ErrorResult) -> Unit = {},
        block: suspend () -> T
    ): T? {
        return try {
            block()
        } catch (e: Exception) {
            val errorResult = handleError(e, operation)
            android.util.Log.e("ErrorHandler", errorResult.logMessage)
            onError(errorResult)
            null
        }
    }
    
    inline fun <T> safeOperation(block: () -> T): T? {
        return try {
            block()
        } catch (e: Exception) {
            android.util.Log.e("ErrorHandler", "Safe operation failed: ${e.message}")
            null
        }
    }
    
    inline fun <T> criticalOperation(
        operation: String,
        fallback: () -> T,
        block: () -> T
    ): T {
        return try {
            block()
        } catch (e: Exception) {
            val errorResult = handleError(e, operation)
            android.util.Log.e("ErrorHandler", "Critical operation failed: ${errorResult.logMessage}")
            fallback()
        }
    }
}