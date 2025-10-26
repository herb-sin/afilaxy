package com.afilaxy.geofence

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.afilaxy.analytics.AnalyticsManager
import com.afilaxy.security.XXEPrevention
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeofenceManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val analyticsManager: AnalyticsManager
) {
    
    private val geofencingClient: GeofencingClient = LocationServices.getGeofencingClient(context)
    
    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(
            context, 
            0, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    fun addHighRiskArea(location: LatLng, radius: Float, id: String = "high_risk_${location.latitude}_${location.longitude}") {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        
        try {
            val geofence = Geofence.Builder()
                .setRequestId(id)
                .setCircularRegion(location.latitude, location.longitude, radius)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                .build()
            
            val geofencingRequest = GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build()
            
            geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)
        } catch (e: SecurityException) {
            SecureLogger.security("GEOFENCE_ADD", "SECURITY_VIOLATION")
        } catch (e: Exception) {
            SecureLogger.e("GeofenceManager", "Error adding geofence", e)
        }
    }
    
    fun setupDefaultGeofences() {
        try {
            // Add common high-risk areas (hospitals, schools, etc.)
            val highRiskAreas = listOf(
                LatLng(-23.5505, -46.6333) to 500f, // São Paulo center
                LatLng(-22.9068, -43.1729) to 500f  // Rio de Janeiro center
            )
            
            highRiskAreas.forEachIndexed { index, (location, radius) ->
                addHighRiskArea(location, radius, "default_area_$index")
            }
        } catch (e: SecurityException) {
            SecureLogger.security("GEOFENCE_SETUP", "SECURITY_VIOLATION")
        } catch (e: Exception) {
            SecureLogger.e("GeofenceManager", "Error setting up geofences", e)
        }
    }
    
    fun removeGeofence(requestId: String) {
        geofencingClient.removeGeofences(listOf(requestId))
    }
    
    fun removeAllGeofences() {
        geofencingClient.removeGeofences(geofencePendingIntent)
    }
}