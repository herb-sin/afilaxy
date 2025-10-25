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
        try {
            analytics = Firebase.analytics
        } catch (e: Exception) {
            // Fallback to no-op analytics if Firebase fails
        }
    }
    
    fun trackEmergencyCreated(location: Location, responseTime: Long) {
        if (!::analytics.isInitialized) return
        try {
            analytics.logEvent("emergency_created") {
                param("response_time_ms", responseTime)
                param("location_accuracy", location.accuracy?.toDouble() ?: 0.0)
            }
        } catch (e: Exception) {
            // Silent fail for analytics
        }
    }
    
    fun trackHelperFound(helperId: String, distance: Double) {
        if (!::analytics.isInitialized) return
        try {
            analytics.logEvent("helper_found") {
                param("distance_meters", distance)
            }
        } catch (e: Exception) {
            // Silent fail for analytics
        }
    }
    
    fun trackHelperRated(helperId: String, rating: Int) {
        if (!::analytics.isInitialized) return
        try {
            analytics.logEvent("helper_rated") {
                param("rating", rating.toLong())
            }
        } catch (e: Exception) {
            // Silent fail for analytics
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