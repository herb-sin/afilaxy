package com.afilaxy.domain.model

data class Location(
    val latitude: Double,
    val longitude: Double,
    val address: String = "",
    val timestamp: Long,
    val accuracy: Float? = null
) {
    constructor(
        latitude: Double,
        longitude: Double,
        accuracy: Float? = null
    ) : this(latitude, longitude, "", System.currentTimeMillis(), accuracy)
}