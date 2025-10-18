package com.afilaxy.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await

object LocationHelper {
    
    private const val TAG = "LocationHelper"
    
    suspend fun getCurrentLocation(context: Context): Location? {
        return try {
            if (!hasLocationPermission(context)) {
                Log.w(TAG, "Permissão de localização não concedida")
                return null
            }
            
            val fusedLocationClient: FusedLocationProviderClient = 
                LocationServices.getFusedLocationProviderClient(context)
            
            val cancellationToken = CancellationTokenSource()
            
            val location = fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationToken.token
            ).await()
            
            Log.d(TAG, "Localização obtida: ${location?.latitude}, ${location?.longitude}")
            location
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao obter localização: ${e.message}")
            null
        }
    }
    
    private fun hasLocationPermission(context: Context): Boolean {
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