package com.afilaxy.domain.usecase

import com.afilaxy.domain.model.ChatMessage
import com.afilaxy.domain.repository.IChatRepository
import com.afilaxy.security.AuthGuard
import javax.inject.Inject

class SendChatMessageUseCase @Inject constructor(
    private val chatRepository: IChatRepository
) {
    
    sealed class Result {
        object Success : Result()
        object AuthenticationRequired : Result()
        object MessageEmpty : Result()
        data class Error(val message: String) : Result()
    }
    
    suspend fun execute(
        emergencyId: String,
        message: String,
        isFromHelper: Boolean = false
    ): Result {
        
        // Validar autenticação
        val userId = AuthGuard.getCurrentUserId()
            ?: return Result.AuthenticationRequired
        
        // Validar mensagem
        if (message.trim().isEmpty()) {
            return Result.MessageEmpty
        }
        
        if (message.length > 500) {
            return Result.Error("Mensagem muito longa (máximo 500 caracteres)")
        }
        
        // Criar mensagem
        val chatMessage = ChatMessage.create(
            emergencyId = emergencyId,
            senderId = userId,
            senderName = if (isFromHelper) "Helper" else "Solicitante",
            message = message.trim(),
            isFromHelper = isFromHelper
        )
        
        // Enviar mensagem
        return chatRepository.sendMessage(chatMessage).fold(
            onSuccess = { Result.Success },
            onFailure = { exception ->
                Result.Error(exception.message ?: "Erro ao enviar mensagem")
            }
        )
    }
}