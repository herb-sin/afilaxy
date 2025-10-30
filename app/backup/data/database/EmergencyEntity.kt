package com.afilaxy.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "emergencies")
data class EmergencyEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val userName: String,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long,
    val status: String,
    val isSynced: Boolean = false
)

@Entity(tableName = "helpers")
data class HelperEntity(
    @PrimaryKey val id: String,
    val nome: String,
    val latitude: Double,
    val longitude: Double,
    val distanciaMetros: Double,
    val isAvailable: Boolean = true,
    val rating: Float = 0f,
    val lastSeen: Long = System.currentTimeMillis()
)