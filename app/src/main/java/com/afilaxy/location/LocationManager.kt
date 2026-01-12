package com.afilaxy.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.tasks.await
import com.afilaxy.security.SecureLogger

class LocationManager(private val context: Context) {
    
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    
    /**
     * Obtém a localização atual uma vez
     */
    suspend fun getCurrentLocation(): LatLng? {
        if (!hasLocationPermission()) {
            SecureLogger.w("LocationManager", "Sem permissão de localização")
            return null
        }
        
        return try {
            SecureLogger.d("LocationManager", "Tentando obter localização...")
            
            // Primeiro tenta a última localização conhecida
            val lastLocation = fusedLocationClient.lastLocation.await()
            if (lastLocation != null) {
                SecureLogger.d("LocationManager", "Localização obtida do cache: ${lastLocation.latitude}, ${lastLocation.longitude}")
                return LatLng(lastLocation.latitude, lastLocation.longitude)
            }
            
            SecureLogger.d("LocationManager", "Cache vazio, solicitando localização atual...")
            
            // Se não há localização prévia, solicita uma nova com timeout
            val currentLocation = fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY, 
                null
            ).await()
            
            if (currentLocation != null) {
                SecureLogger.d("LocationManager", "Localização atual obtida: ${currentLocation.latitude}, ${currentLocation.longitude}")
                LatLng(currentLocation.latitude, currentLocation.longitude)
            } else {
                SecureLogger.w("LocationManager", "Não foi possível obter localização")
                null
            }
        } catch (e: Exception) {
            SecureLogger.e("LocationManager", "Erro ao obter localização: ${e.message}")
            null
        }
    }
    
    /**
     * Verifica se tem permissão de localização
     */
    fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
}