package com.afilaxy.domain.model

data class Helper(
    val id: String,
    val nome: String,
    val distanciaEstimada: String,
    val distanciaMetros: Double = 0.0,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val location: Location = Location(latitude ?: 0.0, longitude ?: 0.0),
    val isAvailable: Boolean = true,
    val tempoResposta: String? = null
)