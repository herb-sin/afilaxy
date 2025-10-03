package com.afilaxy.domain.repository

import com.afilaxy.domain.model.Emergency
import com.afilaxy.domain.model.Helper
import com.afilaxy.domain.model.Location

interface EmergencyRepository {
    suspend fun createEmergency(location: Location): Emergency
    suspend fun findNearbyHelpers(location: Location): List<Helper>
    suspend fun notifyHelpers(helpers: List<Helper>, emergency: Emergency)
}