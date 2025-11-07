package com.afilaxy.domain.repository

import com.google.android.gms.maps.model.LatLng

interface ILocationRepository {
    suspend fun getCurrentLocation(): LatLng?
    fun hasLocationPermission(): Boolean
}