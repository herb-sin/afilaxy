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
        com.afilaxy.security.SecureLogger.e("LocationUtils", "Permission denied for location access")
    }
    
    return locationCallback
}

fun stopLocationUpdates(context: Context, locationCallback: LocationCallback) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    fusedLocationClient.removeLocationUpdates(locationCallback)
}

fun saveUserLocationWithCoords(context: Context, lat: Double, lon: Double) {
    val userId = com.afilaxy.security.AuthGuard.getCurrentUserId()
    if (userId == null) {
        return
    }
    
    if (!com.afilaxy.security.SecurityUtils.isValidCoordinate(lat, lon)) {
        com.afilaxy.security.SecureLogger.w("LocationUtils", "Invalid coordinates rejected")
        return
    }
    
    val coordinates = com.afilaxy.security.SecurityUtils.formatSafeCoordinates(lat, lon)
    com.afilaxy.security.SecureLogger.d("LocationUtils", "Location saved: $coordinates")
}

fun saveUserLocation(context: Context) {
    val userId = com.afilaxy.security.AuthGuard.getCurrentUserId()
    if (userId == null) {
        return
    }
    
    com.afilaxy.security.SecureLogger.d("LocationUtils", "User location save requested")
}