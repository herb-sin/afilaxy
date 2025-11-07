package com.afilaxy.domain.model

data class Helper(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val isActive: Boolean = false,
    val lastUpdate: Long = System.currentTimeMillis(),
    val distance: Double = 0.0 // calculado localmente
)