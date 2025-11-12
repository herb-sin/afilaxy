package com.afilaxy.domain.model

data class ChatMessage(
    val id: String = "",
    val emergencyId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isFromHelper: Boolean = false
) {
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