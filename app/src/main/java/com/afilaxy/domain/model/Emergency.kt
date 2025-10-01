package com.afilaxy.domain.model

data class Emergency(
    val id: String,
    val userId: String,
    val userName: String,
    val location: Location,
    val timestamp: Long,
    val status: EmergencyStatus = EmergencyStatus.ACTIVE,
    val assignedHelperId: String? = null,
    val resolvedAt: Long? = null
) {
    companion object {
        fun create(
            id: String,
            userId: String,
            userName: String,
            location: Location
        ) = Emergency(
            id = id,
            userId = userId,
            userName = userName,
            location = location,
            timestamp = System.currentTimeMillis()
        )
    }
}

enum class EmergencyStatus {
    ACTIVE,
    HELPER_RESPONDING,
    RESOLVED,
    CANCELLED
}