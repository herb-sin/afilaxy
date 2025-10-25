package com.afilaxy.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.app.ActivityCompat
import com.afilaxy.security.AuthGuard
import com.afilaxy.security.SecureLogger
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await

object LocationHelper {
    
    private const val TAG = "LocationHelper"
    
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(context: Context): Location? {
        return try {
            // Authentication check
            if (!AuthGuard.isUserAuthenticated()) {
                SecureLogger.security("LOCATION_ACCESS", "UNAUTHENTICATED")
                return null
            }
            
            // Permission check
            if (!hasLocationPermission(context)) {
                SecureLogger.w(TAG, "Location permission not granted")
                return null
            }
            
            val fusedLocationClient: FusedLocationProviderClient = 
                LocationServices.getFusedLocationProviderClient(context)
            
            val cancellationToken = CancellationTokenSource()
            
            // Permission is checked above, safe to call
            val location = fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationToken.token
            ).await()
            
            SecureLogger.d(TAG, "Location obtained successfully")
            location
            
        } catch (e: SecurityException) {
            SecureLogger.security("LOCATION_ACCESS", "SECURITY_EXCEPTION")
            null
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Error obtaining location", e)
            null
        }
    }
    
    private fun hasLocationPermission(context: Context): Boolean {
        val fineLocationGranted = ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        
        val coarseLocationGranted = ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        
        return fineLocationGranted || coarseLocationGranted
    }
}