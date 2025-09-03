package com.afilaxy.domain.model

data class Helper(
    val id: String,
    val nome: String,
    val distanciaEstimada: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val isAvailable: Boolean = true,
    val responseTime: String? = null
)