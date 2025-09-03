package com.afilaxy.domain.model

data class Emergency(
    val id: String,
    val userId: String,
    val userName: String,
    val location: Location,
    val timestamp: Long = System.currentTimeMillis(),
    val status: EmergencyStatus = EmergencyStatus.ACTIVE,
    val assignedHelperId: String? = null,
    val resolvedAt: Long? = null
)

enum class EmergencyStatus {
    ACTIVE,
    HELPER_RESPONDING,
    RESOLVED,
    CANCELLED
}