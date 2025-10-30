package com.afilaxy.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class LocationManager(private val context: Context) {
    
    private val fusedLocationClient: FusedLocationProviderClient = 
        LocationServices.getFusedLocationProviderClient(context)
    
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    suspend fun getCurrentLocation(): com.afilaxy.domain.model.Location? {
        return try {
            if (!hasLocationPermission()) {
                Log.w("LocationManager", "Location permission not granted")
                return null
            }
            
            val location = fusedLocationClient.lastLocation.await()
            location?.let {
                com.afilaxy.domain.model.Location(it.latitude, it.longitude)
            }
        } catch (e: Exception) {
            Log.e("LocationManager", "Error getting location", e)
            null
        }
    }
    
    suspend fun saveUserLocation(): Boolean {
        return try {
            val currentUser = auth.currentUser ?: return false
            val location = getCurrentLocation() ?: return false
            
            val locationData = mapOf(
                "latitude" to location.latitude,
                "longitude" to location.longitude,
                "timestamp" to System.currentTimeMillis(),
                "userId" to currentUser.uid
            )
            
            firestore.collection("user_locations")
                .document(currentUser.uid)
                .set(locationData)
                .await()
            
            Log.d("LocationManager", "Location saved: ${location.latitude}, ${location.longitude}")
            true
        } catch (e: Exception) {
            Log.e("LocationManager", "Error saving location", e)
            false
        }
    }
    
    suspend fun findNearbyUsers(radiusKm: Double = 5.0): List<NearbyUser> {
        return try {
            val currentLocation = getCurrentLocation() ?: return emptyList()
            val currentUser = auth.currentUser ?: return emptyList()
            
            // Limitar busca para melhor performance
            val snapshot = firestore.collection("user_locations")
                .limit(15) // Limitar resultados
                .get()
                .await()
            
            val nearbyUsers = mutableListOf<NearbyUser>()
            
            for (document in snapshot.documents) {
                val userId = document.getString("userId") ?: continue
                if (userId == currentUser.uid) continue
                
                val lat = document.getDouble("latitude") ?: continue
                val lng = document.getDouble("longitude") ?: continue
                
                val distance = calculateDistance(
                    currentLocation.latitude, currentLocation.longitude,
                    lat, lng
                )
                
                if (distance <= radiusKm) {
                    nearbyUsers.add(
                        NearbyUser(
                            userId = userId,
                            latitude = lat,
                            longitude = lng,
                            distanceKm = distance
                        )
                    )
                }
            }
            
            nearbyUsers.sortedBy { it.distanceKm }.take(10) // Máximo 10 usuários
        } catch (e: Exception) {
            Log.e("LocationManager", "Error finding nearby users", e)
            emptyList()
        }
    }
    
    private fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ActivityCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return (results[0] / 1000.0) // Convert to kilometers
    }
}

data class NearbyUser(
    val userId: String,
    val latitude: Double,
    val longitude: Double,
    val distanceKm: Double
)