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

// Instâncias Firebase reutilizáveis para melhor performance
private val firebaseAuth by lazy { FirebaseAuth.getInstance() }
private val firebaseFirestore by lazy { FirebaseFirestore.getInstance() }

@SuppressLint("MissingPermission")
fun saveUserLocation(context: Context) {
    android.util.Log.d("LocationUtils", "Iniciando salvamento de localização")
    
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    val user = firebaseAuth.currentUser
    
    if (!AuthValidator.isUserAuthenticated()) {
        android.util.Log.w("LocationUtils", "Usuário não autenticado - operação cancelada")
        return
    }
    
    fusedLocationClient.lastLocation
        .addOnSuccessListener { location ->
            if (location != null) {
                android.util.Log.d("LocationUtils", "Localização obtida")
                
                val db = firebaseFirestore
                val locationData = mapOf<String, Any>(
                    "location" to com.google.firebase.firestore.GeoPoint(location.latitude, location.longitude),
                    "lastLocationUpdate" to System.currentTimeMillis()
                )
                
                db.collection("users").document(user!!.uid)
                    .update(locationData)
                    .addOnSuccessListener {
                        android.util.Log.d("LocationUtils", "Localização salva no Firestore")
                    }
                    .addOnFailureListener { e ->
                        android.util.Log.e("LocationUtils", "Erro ao salvar localização: ${InputSanitizer.sanitizeForLog(e.message)}")
                    }
            } else {
                android.util.Log.w("LocationUtils", "Localização nula, tentando obter nova localização")
                user?.let { requestNewLocation(context, it) }
            }
        }
        .addOnFailureListener { e ->
            android.util.Log.e("LocationUtils", "Erro ao obter localização: ${InputSanitizer.sanitizeForLog(e.message)}")
        }
}

@SuppressLint("MissingPermission")
fun requestNewLocation(context: Context, user: com.google.firebase.auth.FirebaseUser?) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY, 5000L
    ).setMaxUpdates(1).build()
    
    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { location ->
                android.util.Log.d("LocationUtils", "Nova localização obtida")
                
                val db = firebaseFirestore
                val locationData = mapOf<String, Any>(
                    "location" to com.google.firebase.firestore.GeoPoint(location.latitude, location.longitude),
                    "lastLocationUpdate" to System.currentTimeMillis()
                )
                
                user?.let { u ->
                    db.collection("users").document(u.uid).update(locationData)
                        .addOnFailureListener { e ->
                            android.util.Log.e("LocationUtils", "Erro ao atualizar localização: ${InputSanitizer.sanitizeForLog(e.message)}")
                        }
                }
                
                // Remover callback após uso
                fusedLocationClient.removeLocationUpdates(this)
            }
        }
    }
    
    fusedLocationClient.requestLocationUpdates(
        locationRequest,
        locationCallback,
        Looper.getMainLooper()
    )
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
    ErrorHandler.safeCall(
        operation = "saveUserLocationWithCoords",
        onError = { error ->
            android.util.Log.w("LocationUtils", "Falha ao salvar localização: ${error.userMessage}")
        }
    ) {
        val user = AuthValidator.requireAuthentication()
        val db = firebaseFirestore
        val locationData = mapOf<String, Any>(
            "location" to com.google.firebase.firestore.GeoPoint(latitude, longitude),
            "lastLocationUpdate" to System.currentTimeMillis()
        )
        db.collection("users").document(user.uid).update(locationData)
            .addOnFailureListener { e ->
                val errorResult = ErrorHandler.handleError(e, "updateLocation")
                android.util.Log.e("LocationUtils", errorResult.logMessage)
            }
    }
}