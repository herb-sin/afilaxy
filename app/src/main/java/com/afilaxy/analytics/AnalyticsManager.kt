package com.afilaxy.analytics

import com.afilaxy.domain.model.Location
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsManager @Inject constructor() {
    
    private lateinit var analytics: FirebaseAnalytics
    
    fun initialize() {
        analytics = Firebase.analytics
    }
    
    fun trackEmergencyCreated(location: Location, responseTime: Long) {
        analytics.logEvent("emergency_created") {
            param("response_time_ms", responseTime)
            param("location_accuracy", location.accuracy?.toDouble() ?: 0.0)
            param("latitude", location.latitude)
            param("longitude", location.longitude)
        }
    }
    
    fun trackHelperFound(helperId: String, distance: Double) {
        analytics.logEvent("helper_found") {
            param("helper_id", helperId)
            param("distance_meters", distance)
        }
    }
    
    fun trackHelperRated(helperId: String, rating: Int) {
        analytics.logEvent("helper_rated") {
            param("helper_id", helperId)
            param("rating", rating.toLong())
        }
    }
    
    fun trackEmergencyResolved(emergencyId: String, resolutionTime: Long) {
        analytics.logEvent("emergency_resolved") {
            param("emergency_id", emergencyId)
            param("resolution_time_ms", resolutionTime)
        }
    }
    
    fun trackLocationPermissionGranted() {
        analytics.logEvent("location_permission_granted") {}
    }
    
    fun trackLocationPermissionDenied() {
        analytics.logEvent("location_permission_denied") {}
    }
    
    fun trackOfflineMode() {
        analytics.logEvent("offline_mode_activated") {}
    }
    
    fun trackGeofenceEntered(geofenceId: String) {
        analytics.logEvent("geofence_entered") {
            param("geofence_id", geofenceId)
        }
    }
}