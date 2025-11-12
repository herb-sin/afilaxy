package com.afilaxy.domain.repository

import com.afilaxy.domain.model.ChatMessage
import kotlinx.coroutines.flow.Flow

interface IChatRepository {
    suspend fun sendMessage(message: ChatMessage): Result<Unit>
    fun getMessages(emergencyId: String): Flow<List<ChatMessage>>
    suspend fun clearChat(emergencyId: String): Result<Unit>
}