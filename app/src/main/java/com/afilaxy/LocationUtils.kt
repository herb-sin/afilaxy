package com.afilaxy

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@SuppressLint("MissingPermission")
fun saveUserLocation(context: Context) {
    android.util.Log.d("LocationUtils", "📍 Iniciando salvamento de localização")
    
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    val user = FirebaseAuth.getInstance().currentUser
    
    if (user == null) {
        android.util.Log.w("LocationUtils", "⚠️ Usuário não autenticado, usando localização simulada")
        // Salvar localização simulada para teste
        saveSimulatedLocation()
        return
    }
    
    fusedLocationClient.lastLocation
        .addOnSuccessListener { location ->
            if (location != null) {
                android.util.Log.d("LocationUtils", "✅ Localização obtida: ${location.latitude}, ${location.longitude}")
                
                val db = FirebaseFirestore.getInstance()
                val locationData = mapOf<String, Any>(
                    "location" to com.google.firebase.firestore.GeoPoint(location.latitude, location.longitude),
                    "lastLocationUpdate" to System.currentTimeMillis()
                )
                
                db.collection("users").document(user.uid)
                    .update(locationData)
                    .addOnSuccessListener {
                        android.util.Log.d("LocationUtils", "✅ Localização salva no Firestore")
                    }
                    .addOnFailureListener { e ->
                        android.util.Log.e("LocationUtils", "❌ Erro ao salvar localização: ${e.message}")
                    }
            } else {
                android.util.Log.w("LocationUtils", "⚠️ Localização nula, tentando obter nova localização")
                requestNewLocation(context, user)
            }
        }
        .addOnFailureListener { e ->
            android.util.Log.e("LocationUtils", "❌ Erro ao obter localização: ${e.message}")
            saveSimulatedLocation()
        }
}

@SuppressLint("MissingPermission")
fun requestNewLocation(context: Context, user: com.google.firebase.auth.FirebaseUser) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY, 5000L
    ).setMaxUpdates(1).build()
    
    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { location ->
                android.util.Log.d("LocationUtils", "✅ Nova localização obtida: ${location.latitude}, ${location.longitude}")
                
                val db = FirebaseFirestore.getInstance()
                val locationData = mapOf<String, Any>(
                    "location" to com.google.firebase.firestore.GeoPoint(location.latitude, location.longitude),
                    "lastLocationUpdate" to System.currentTimeMillis()
                )
                
                db.collection("users").document(user.uid).update(locationData)
            }
        }
    }
    
    fusedLocationClient.requestLocationUpdates(
        locationRequest,
        locationCallback,
        Looper.getMainLooper()
    )
}

fun saveSimulatedLocation() {
    android.util.Log.d("LocationUtils", "🎯 Salvando localização simulada para teste")
    // Para teste: criar localizações simuladas diferentes
    val simulatedLat = -23.6200 + (Math.random() * 0.01) // Variação de ~1km
    val simulatedLon = -46.6700 + (Math.random() * 0.01)
    
    android.util.Log.d("LocationUtils", "📍 Localização simulada: $simulatedLat, $simulatedLon")
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
        val locationData = mapOf<String, Any>(
            "location" to com.google.firebase.firestore.GeoPoint(latitude, longitude),
            "lastLocationUpdate" to System.currentTimeMillis()
        )
        // Atualizar documento do usuário na coleção users
        db.collection("users").document(user.uid).update(locationData)
    }
}