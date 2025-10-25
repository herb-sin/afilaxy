package com.afilaxy.domain.repository

import com.afilaxy.domain.model.Emergency
import com.afilaxy.domain.model.Helper
import com.afilaxy.domain.model.Location

interface EmergencyRepository {
    suspend fun createEmergency(location: Location): Emergency
    suspend fun findNearbyHelpers(location: Location): List<Helper>
    suspend fun notifyHelpers(helpers: List<Helper>, emergency: Emergency)
    
    // Safe operations with error handling
    suspend fun createEmergencySafe(location: Location): Result<Emergency> {
        return try {
            Result.success(createEmergency(location))
        } catch (e: Exception) {
            android.util.Log.e("EmergencyRepository", "Error creating emergency", e)
            Result.failure(e)
        }
    }
    
    suspend fun findNearbyHelpersSafe(location: Location): Result<List<Helper>> {
        return try {
            Result.success(findNearbyHelpers(location))
        } catch (e: Exception) {
            android.util.Log.e("EmergencyRepository", "Error finding helpers", e)
            Result.failure(e)
        }
    }
}