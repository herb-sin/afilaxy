package com.afilaxy.domain.repository

import com.afilaxy.domain.model.Emergency
import com.afilaxy.domain.model.Helper
import com.afilaxy.domain.model.Location

interface EmergencyRepository {
    suspend fun createEmergency(emergency: Emergency): Result<String>
    suspend fun findNearbyHelpers(location: Location, radiusKm: Double): Result<List<Helper>>
    suspend fun updateEmergencyStatus(emergencyId: String, status: String): Result<Unit>
}