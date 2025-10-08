package com.afilaxy

import android.content.Context
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority

fun startSignificantMovementUpdates(
    context: Context,
    minDistanceMeters: Float,
    onLocationUpdate: (Double, Double) -> Unit
): LocationCallback {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    
    val locationRequest = LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 30000)
        .setMinUpdateDistanceMeters(minDistanceMeters)
        .build()
    
    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let { location ->
                onLocationUpdate(location.latitude, location.longitude)
            }
        }
    }
    
    try {
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    } catch (e: SecurityException) {
        com.afilaxy.security.SecurityUtils.safeLog("LocationUtils", "Permission denied for location access", com.afilaxy.security.SecurityUtils.LogLevel.ERROR)
    }
    
    return locationCallback
}

fun stopLocationUpdates(context: Context, locationCallback: LocationCallback) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    fusedLocationClient.removeLocationUpdates(locationCallback)
}

fun saveUserLocationWithCoords(context: Context, lat: Double, lon: Double) {
    if (!com.afilaxy.security.SecureValidator.requireAuthentication("saveUserLocationWithCoords")) {
        return
    }
    
    if (!com.afilaxy.security.SecureValidator.validateCoordinates(lat, lon)) {
        com.afilaxy.security.SecurityUtils.safeLog("LocationUtils", "Invalid coordinates rejected", com.afilaxy.security.SecurityUtils.LogLevel.WARN)
        return
    }
    
    val coordinates = com.afilaxy.security.SecurityUtils.formatSafeCoordinates(lat, lon)
    com.afilaxy.security.SecurityUtils.safeLog("LocationUtils", "Location saved: $coordinates")
}

fun saveUserLocation(context: Context) {
    if (!com.afilaxy.security.SecureValidator.requireAuthentication("saveUserLocation")) {
        return
    }
    
    com.afilaxy.security.SecurityUtils.safeLog("LocationUtils", "User location save requested")
}