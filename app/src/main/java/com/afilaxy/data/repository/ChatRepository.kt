package com.afilaxy.data.repository

import com.afilaxy.domain.repository.IChatRepository
import com.afilaxy.domain.model.ChatMessage
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import com.afilaxy.security.SecureLogger

class ChatRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : IChatRepository {
    
    override suspend fun sendMessage(message: ChatMessage): Result<Unit> {
        return try {
            val messageData = hashMapOf(
                "senderId" to message.senderId,
                "message" to message.message,
                "timestamp" to message.timestamp
            )
            
            firestore.collection("emergency_chats")
                .document(message.emergencyId)
                .collection("messages")
                .add(messageData)
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            SecureLogger.e("ChatRepository", "Erro ao enviar mensagem", e)
            Result.failure(e)
        }
    }
    
    suspend fun sendMessage(emergencyId: String, senderId: String, message: String): Boolean {
        val chatMessage = ChatMessage(
            emergencyId = emergencyId,
            senderId = senderId,
            message = message,
            timestamp = System.currentTimeMillis()
        )
        return sendMessage(chatMessage).isSuccess
    }
    
    override fun getMessages(emergencyId: String): Flow<List<ChatMessage>> = callbackFlow {
        val listener = firestore.collection("emergency_chats")
            .document(emergencyId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    SecureLogger.e("ChatRepository", "Erro ao escutar mensagens", error)
                    return@addSnapshotListener
                }
                
                val messages = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        ChatMessage(
                            id = doc.id,
                            emergencyId = emergencyId,
                            senderId = doc.getString("senderId") ?: "",
                            message = doc.getString("message") ?: "",
                            timestamp = doc.getLong("timestamp") ?: 0L
                        )
                    } catch (e: Exception) {
                        SecureLogger.e("ChatRepository", "Erro ao mapear mensagem", e)
                        null
                    }
                } ?: emptyList()
                
                trySend(messages)
            }
        
        awaitClose { listener.remove() }
    }
    
    fun getChatMessages(emergencyId: String): Flow<List<ChatMessageEntity>> = callbackFlow {
        val listener = firestore.collection("emergency_chats")
            .document(emergencyId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    SecureLogger.e("ChatRepository", "Erro ao escutar mensagens", error)
                    return@addSnapshotListener
                }
                
                val messages = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        ChatMessageEntity(
                            id = doc.id,
                            senderId = doc.getString("senderId") ?: "",
                            message = doc.getString("message") ?: "",
                            timestamp = doc.getLong("timestamp") ?: 0L
                        )
                    } catch (e: Exception) {
                        SecureLogger.e("ChatRepository", "Erro ao mapear mensagem", e)
                        null
                    }
                } ?: emptyList()
                
                trySend(messages)
            }
        
        awaitClose { listener.remove() }
    }
    
    override suspend fun clearChat(emergencyId: String): Result<Unit> {
        return try {
            val messages = firestore.collection("emergency_chats")
                .document(emergencyId)
                .collection("messages")
                .get()
                .await()
            
            messages.documents.forEach { it.reference.delete() }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

data class ChatMessageEntity(
    val id: String = "",
    val senderId: String = "",
    val message: String = "",
    val timestamp: Long = 0L
)