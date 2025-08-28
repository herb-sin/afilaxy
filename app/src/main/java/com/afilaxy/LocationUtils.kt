package com.afilaxy

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@SuppressLint("MissingPermission")
fun saveUserLocation(context: Context) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
        val user = FirebaseAuth.getInstance().currentUser
        if (location != null && user != null) {
            val db = FirebaseFirestore.getInstance()
            val userLocation = hashMapOf(
                "uid" to user.uid,
                "email" to user.email,
                "latitude" to location.latitude,
                "longitude" to location.longitude,
                "timestamp" to System.currentTimeMillis()
            )
            db.collection("user_locations").document(user.uid).set(userLocation)
        }
    }
}

fun startSignificantMovementUpdates(
    context: Context,
    minDistanceMeters: Float = 50f,
    onLocationChanged: (latitude: Double, longitude: Double) -> Unit
): LocationCallback {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY, 10_000L
    ).setMinUpdateDistanceMeters(minDistanceMeters).build()

    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { location ->
                onLocationChanged(location.latitude, location.longitude)
            }
        }
    }

    fusedLocationClient.requestLocationUpdates(
        locationRequest,
        locationCallback,
        Looper.getMainLooper()
    )
    return locationCallback
}

fun stopLocationUpdates(context: Context, callback: LocationCallback) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    fusedLocationClient.removeLocationUpdates(callback)
}

fun saveUserLocationWithCoords(context: Context, latitude: Double, longitude: Double) {
    val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
    if (user != null) {
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        val userLocation = hashMapOf(
            "uid" to user.uid,
            "email" to user.email,
            "latitude" to latitude,
            "longitude" to longitude,
            "timestamp" to System.currentTimeMillis()
        )
        db.collection("user_locations").document(user.uid).set(userLocation)
    }
}