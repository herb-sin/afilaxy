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
    fun getCurrentLocation(context: Context, callback: (Double?, Double?) -> Unit) {
        try {
            Log.d("LocationUtils", "Getting current location...")
            val locationManager = LocationManager(context)
            
            CoroutineScope(Dispatchers.IO).launch {
                val location = locationManager.getCurrentLocation()
                if (location != null) {
                    Log.d("LocationUtils", "Location obtained successfully")
                    callback(location.latitude, location.longitude)
                } else {
                    Log.w("LocationUtils", "Failed to get location")
                    callback(null, null)
                }
            }
        } catch (e: Exception) {
            Log.e("LocationUtils", "Error getting location", e)
            callback(null, null)
        }
    }
}