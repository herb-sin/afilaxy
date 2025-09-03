package com.afilaxy.domain.repository

import com.afilaxy.domain.model.Location

interface LocationRepository {
    suspend fun getCurrentLocation(): Location?
    suspend fun saveUserLocation(location: Location)
    suspend fun startLocationUpdates(callback: (Location) -> Unit)
    suspend fun stopLocationUpdates()
}