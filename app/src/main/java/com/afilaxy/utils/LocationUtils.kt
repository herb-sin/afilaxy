package com.afilaxy.utils

import android.content.Context
import android.util.Log
import com.afilaxy.location.LocationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Location utilities
 */
object LocationUtils {
    fun saveUserLocation(context: Context) {
        try {
            Log.d("LocationUtils", "Saving user location...")
            val locationManager = LocationManager(context)
            
            CoroutineScope(Dispatchers.IO).launch {
                val success = locationManager.saveUserLocation()
                if (success) {
                    Log.d("LocationUtils", "Location saved successfully")
                } else {
                    Log.w("LocationUtils", "Failed to save location")
                }
            }
        } catch (e: Exception) {
            Log.e("LocationUtils", "Error saving location", e)
        }
    }
}