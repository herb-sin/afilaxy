package com.afilaxy.domain.model

data class EmergencyRequest(
    val id: String = "",
    val requesterId: String = "",
    val requesterName: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis(),
    val isActive: Boolean = true,
    val expiresAt: Long = System.currentTimeMillis() + TIMEOUT_DURATION_MS
) {
    companion object {
        const val TIMEOUT_DURATION_MS = 5 * 60 * 1000L // 5 minutos
    }
    
    fun isExpired(): Boolean = System.currentTimeMillis() > expiresAt
}