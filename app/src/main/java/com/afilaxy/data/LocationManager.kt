package com.afilaxy.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.tasks.await

object LocationManager {
    
    suspend fun getCurrentLocation(context: Context): Pair<Double, Double>? {
        return try {
            android.util.Log.d("LocationManager", "Tentando obter localização...")
            
            if (!hasLocationPermission(context)) {
                android.util.Log.w("LocationManager", "Sem permissão de localização, usando fallback")
                return Pair(-23.5505, -46.6333) // Fallback São Paulo
            }
            
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            
            // Tentar última localização conhecida primeiro
            val lastLocation = fusedLocationClient.lastLocation.await()
            if (lastLocation != null) {
                android.util.Log.d("LocationManager", "Localização obtida do cache: ${lastLocation.latitude}, ${lastLocation.longitude}")
                return Pair(lastLocation.latitude, lastLocation.longitude)
            }
            
            // Solicitar localização atual
            val currentLocation = fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY, null
            ).await()
            
            if (currentLocation != null) {
                android.util.Log.d("LocationManager", "Localização atual obtida: ${currentLocation.latitude}, ${currentLocation.longitude}")
                Pair(currentLocation.latitude, currentLocation.longitude)
            } else {
                android.util.Log.w("LocationManager", "Não foi possível obter localização, usando fallback")
                Pair(-23.5505, -46.6333) // Fallback
            }
            
        } catch (e: Exception) {
            android.util.Log.e("LocationManager", "Erro ao obter localização", e)
            Pair(-23.5505, -46.6333) // Fallback
        }
    }
    
    private fun hasLocationPermission(context: Context): Boolean {
        return ActivityCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ActivityCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
}