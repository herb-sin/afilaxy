package com.afilaxy.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Gerenciador de chat simplificado
 */
object ChatManager {
    
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    /**
     * Envia mensagem no chat
     */
    suspend fun sendMessage(emergencyId: String, message: String): Boolean {
        return try {
            val userId = auth.currentUser?.uid ?: return false
            
            val messageData = mapOf(
                "senderId" to userId,
                "message" to message,
                "timestamp" to System.currentTimeMillis()
            )
            
            firestore.collection("emergency_chats")
                .document(emergencyId)
                .collection("messages")
                .add(messageData)
                .await()
            
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Escuta mensagens do chat em tempo real
     */
    fun listenToMessages(emergencyId: String): Flow<List<SimpleMessage>> = callbackFlow {
        val listener = firestore.collection("emergency_chats")
            .document(emergencyId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                
                val messages = snapshot?.documents?.mapNotNull { doc ->
                    val senderId = doc.getString("senderId") ?: return@mapNotNull null
                    val message = doc.getString("message") ?: return@mapNotNull null
                    val timestamp = doc.getLong("timestamp") ?: 0L
                    
                    SimpleMessage(
                        id = doc.id,
                        senderId = senderId,
                        message = message,
                        timestamp = timestamp,
                        isFromCurrentUser = senderId == auth.currentUser?.uid
                    )
                } ?: emptyList()
                
                trySend(messages)
            }
        
        awaitClose { listener.remove() }
    }
}

data class SimpleMessage(
    val id: String,
    val senderId: String,
    val message: String,
    val timestamp: Long,
    val isFromCurrentUser: Boolean
)