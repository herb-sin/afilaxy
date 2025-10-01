package com.afilaxy.domain.model

data class HealthData(
    val userId: String,
    val condition: HealthCondition,
    val severity: Severity,
    val medications: List<String> = emptyList(),
    val emergencyContact: String? = null,
    val allergies: List<String> = emptyList(),
    val lastUpdated: Long
) {
    companion object {
        fun create(
            userId: String,
            condition: HealthCondition,
            severity: Severity,
            medications: List<String> = emptyList(),
            emergencyContact: String? = null,
            allergies: List<String> = emptyList()
        ) = HealthData(
            userId = userId,
            condition = condition,
            severity = severity,
            medications = medications,
            emergencyContact = emergencyContact,
            allergies = allergies,
            lastUpdated = System.currentTimeMillis()
        )
    }
}

enum class HealthCondition {
    ASMA,
    DPOC,
    ASMA_E_DPOC,
    OUTROS
}

enum class Severity {
    LEVE,
    MODERADA,
    GRAVE,
    MUITO_GRAVE
}