package com.afilaxy.data.repository

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.afilaxy.domain.repository.ILocationRepository
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : ILocationRepository {
    
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    
    override suspend fun getCurrentLocation(): LatLng? {
        if (!hasLocationPermission()) return null
        
        return try {
            // Tenta última localização conhecida primeiro
            val lastLocation = fusedLocationClient.lastLocation.await()
            if (lastLocation != null) {
                return LatLng(lastLocation.latitude, lastLocation.longitude)
            }
            
            // Solicita localização atual
            val currentLocation = fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY, null
            ).await()
            
            currentLocation?.let { LatLng(it.latitude, it.longitude) }
        } catch (e: Exception) {
            null
        }
    }
    
    override fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ActivityCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
}