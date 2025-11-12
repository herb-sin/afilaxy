package com.afilaxy.data.repository

import com.afilaxy.domain.model.ChatMessage
import com.afilaxy.domain.repository.IChatRepository
import com.afilaxy.security.InputSanitizer
import com.afilaxy.security.AuthGuard
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ChatRepository : IChatRepository {
    
    private val firestore = FirebaseFirestore.getInstance()
    private val chatCollection = firestore.collection("emergency_chats")
    
    override suspend fun sendMessage(message: ChatMessage): Result<Unit> {
        return try {
            // Validar autenticação
            val authResult = AuthGuard.requireAuthentication("send_chat_message")
            if (authResult !is com.afilaxy.security.AuthResult.Authenticated) {
                return Result.failure(Exception("Authentication required"))
            }
            
            // Sanitizar mensagem
            val sanitizedMessage = message.copy(
                message = InputSanitizer.sanitizeText(message.message)
            )
            
            chatCollection
                .document(message.emergencyId)
                .collection("messages")
                .document(message.id)
                .set(sanitizedMessage)
                .await()
                
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("ChatRepository", "Error sending message: ${e.javaClass.simpleName}")
            Result.failure(e)
        }
    }
    
    override fun getMessages(emergencyId: String): Flow<List<ChatMessage>> = callbackFlow {
        val listener = chatCollection
            .document(emergencyId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("ChatRepository", "Error listening to messages: ${error.javaClass.simpleName}")
                    return@addSnapshotListener
                }
                
                val messages = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(ChatMessage::class.java)
                    } catch (e: Exception) {
                        android.util.Log.w("ChatRepository", "Error parsing message: ${e.javaClass.simpleName}")
                        null
                    }
                } ?: emptyList()
                
                trySend(messages)
            }
        
        awaitClose { listener.remove() }
    }
    
    override suspend fun clearChat(emergencyId: String): Result<Unit> {
        return try {
            val authResult = AuthGuard.requireAuthentication("clear_chat")
            if (authResult !is com.afilaxy.security.AuthResult.Authenticated) {
                return Result.failure(Exception("Authentication required"))
            }
            
            val messagesRef = chatCollection
                .document(emergencyId)
                .collection("messages")
            
            val messages = messagesRef.get().await()
            messages.documents.forEach { doc ->
                doc.reference.delete()
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("ChatRepository", "Error clearing chat: ${e.javaClass.simpleName}")
            Result.failure(e)
        }
    }
}