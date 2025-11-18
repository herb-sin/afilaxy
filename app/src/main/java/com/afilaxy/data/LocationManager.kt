package com.afilaxy.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.afilaxy.performance.LogOptimizer
import com.google.android.gms.location.*
import kotlinx.coroutines.tasks.await

object LocationManager {
    
    suspend fun getCurrentLocation(context: Context): Pair<Double, Double>? {
        return try {
            LogOptimizer.d("LocationManager", "Tentando obter localização...")
            
            if (!hasLocationPermission(context)) {
                LogOptimizer.w("LocationManager", "Sem permissão de localização")
                return null // Retornar null quando não tem permissão
            }
            
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            
            // Tentar última localização conhecida primeiro
            val lastLocation = fusedLocationClient.lastLocation.await()
            if (lastLocation != null && isValidLocation(lastLocation.latitude, lastLocation.longitude)) {
                LogOptimizer.d("LocationManager", "Localização obtida do cache: ${lastLocation.latitude}, ${lastLocation.longitude}")
                return Pair(lastLocation.latitude, lastLocation.longitude)
            }
            
            LogOptimizer.d("LocationManager", "Solicitando localização atual com timeout estendido...")
            
            // Solicitar localização atual com timeout maior para dispositivos físicos
            val currentLocation = try {
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY, 
                    null
                ).await()
            } catch (e: Exception) {
                LogOptimizer.w("LocationManager", "Timeout ou erro ao obter GPS: ${e.message}")
                null
            }
            
            if (currentLocation != null && isValidLocation(currentLocation.latitude, currentLocation.longitude)) {
                LogOptimizer.d("LocationManager", "Localização atual obtida: ${currentLocation.latitude}, ${currentLocation.longitude}")
                Pair(currentLocation.latitude, currentLocation.longitude)
            } else {
                LogOptimizer.w("LocationManager", "Não foi possível obter localização válida, usando fallback")
                Pair(-23.5505, -46.6333) // Fallback
            }
            
        } catch (e: Exception) {
            LogOptimizer.e("LocationManager", "Erro ao obter localização", e)
            Pair(-23.5505, -46.6333) // Fallback
        }
    }
    
    private fun isValidLocation(lat: Double, lng: Double): Boolean {
        // Verifica se não é a localização padrão de São Paulo
        return !(lat == -23.5505 && lng == -46.6333) && 
               lat != 0.0 && lng != 0.0 &&
               lat >= -90 && lat <= 90 &&
               lng >= -180 && lng <= 180
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