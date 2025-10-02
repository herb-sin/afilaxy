package com.afilaxy

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.afilaxy.security.AuthValidator
import com.afilaxy.security.InputSanitizer
import com.afilaxy.utils.ErrorHandler

private val firebaseAuth by lazy { FirebaseAuth.getInstance() }
private val firebaseFirestore by lazy { FirebaseFirestore.getInstance() }

@SuppressLint("MissingPermission")
fun saveUserLocation(context: Context) {
    ErrorHandler.safeOperation {
        val user = AuthValidator.requireAuthentication()
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    val locationData = mapOf<String, Any>(
                        "location" to com.google.firebase.firestore.GeoPoint(location.latitude, location.longitude),
                        "lastLocationUpdate" to System.currentTimeMillis()
                    )
                    
                    firebaseFirestore.collection("users").document(user.uid)
                        .update(locationData)
                        .addOnFailureListener { e ->
                            android.util.Log.e("LocationUtils", "Erro: ${InputSanitizer.sanitizeForLog(e.message)}")
                        }
                } else {
                    requestNewLocation(context, user)
                }
            }
    }
}

@SuppressLint("MissingPermission")
fun requestNewLocation(context: Context, user: com.google.firebase.auth.FirebaseUser) {
    ErrorHandler.safeOperation {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L)
            .setMaxUpdates(1).build()
        
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    val locationData = mapOf<String, Any>(
                        "location" to com.google.firebase.firestore.GeoPoint(location.latitude, location.longitude),
                        "lastLocationUpdate" to System.currentTimeMillis()
                    )
                    
                    firebaseFirestore.collection("users").document(user.uid).update(locationData)
                    fusedLocationClient.removeLocationUpdates(this)
                }
            }
        }
        
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }
}

@SuppressLint("MissingPermission")
fun startSignificantMovementUpdates(
    context: Context,
    minDistanceMeters: Float = 50f,
    onLocationChanged: (latitude: Double, longitude: Double) -> Unit
): LocationCallback? {
    return ErrorHandler.safeOperation {
        AuthValidator.requireAuthentication()
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10_000L)
            .setMinUpdateDistanceMeters(minDistanceMeters).build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    onLocationChanged(location.latitude, location.longitude)
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        locationCallback
    }
}

fun stopLocationUpdates(context: Context, callback: LocationCallback) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    fusedLocationClient.removeLocationUpdates(callback)
}

fun saveUserLocationWithCoords(context: Context, latitude: Double, longitude: Double) {
    ErrorHandler.safeCall(
        operation = "saveUserLocationWithCoords",
        onError = { error ->
            android.util.Log.w("LocationUtils", "Falha: ${error.userMessage}")
        }
    ) {
        val user = AuthValidator.requireAuthentication()
        val locationData = mapOf<String, Any>(
            "location" to com.google.firebase.firestore.GeoPoint(latitude, longitude),
            "lastLocationUpdate" to System.currentTimeMillis()
        )
        firebaseFirestore.collection("users").document(user.uid).update(locationData)
    }
}