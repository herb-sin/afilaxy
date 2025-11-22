package com.afilaxy.domain.repository

import com.afilaxy.domain.model.Emergency
import com.afilaxy.domain.model.Helper
import com.afilaxy.domain.model.Location

interface EmergencyRepository {
    suspend fun createEmergency(emergency: Emergency): Result<String>
    suspend fun createEmergency(latitude: Double, longitude: Double): Result<String>
    suspend fun cancelEmergency(emergencyId: String): Result<Boolean>
    suspend fun activateHelper(latitude: Double, longitude: Double): Result<Boolean>
    suspend fun deactivateHelper(): Result<Boolean>
    suspend fun acceptEmergency(emergencyId: String): Result<Boolean>
    suspend fun getActiveEmergency(): Result<String?>
    suspend fun clearUserEmergencies(): Result<Boolean>
    suspend fun isHelperActive(): Result<Boolean>
    suspend fun findNearbyHelpers(location: Location, radiusKm: Double): Result<List<Helper>>
    suspend fun updateEmergencyStatus(emergencyId: String, status: String): Result<Unit>
    suspend fun finishEmergency(emergencyId: String): Result<Boolean>
}