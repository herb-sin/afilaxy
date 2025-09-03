package com.afilaxy.domain.repository

import com.afilaxy.domain.model.Emergency
import com.afilaxy.domain.model.Helper
import com.afilaxy.domain.model.Location

interface EmergencyRepository {
    suspend fun findNearbyHelpers(location: Location, radiusKm: Double = 5.0): List<Helper>
    suspend fun createEmergency(emergency: Emergency): String
    suspend fun updateEmergencyStatus(emergencyId: String, status: String)
    suspend fun sendEmergencyAlert(helperId: String, emergency: Emergency)
}