package com.afilaxy.domain.model

data class ChatMessage(
    @JvmField val id: String = "",
    @JvmField val emergencyId: String = "",
    @JvmField val senderId: String = "",
    @JvmField val senderName: String = "",
    @JvmField val message: String = "",
    @JvmField val timestamp: Long = System.currentTimeMillis(),
    @JvmField val isFromHelper: Boolean = false
) {
    // No-argument constructor for Firestore
    constructor() : this("", "", "", "", "", System.currentTimeMillis(), false)
    
    companion object {
        fun create(
            emergencyId: String,
            senderId: String,
            senderName: String,
            message: String,
            isFromHelper: Boolean = false
        ) = ChatMessage(
            id = "${System.currentTimeMillis()}_${senderId.take(8)}",
            emergencyId = emergencyId,
            senderId = senderId,
            senderName = senderName,
            message = message,
            isFromHelper = isFromHelper
        )
    }
}