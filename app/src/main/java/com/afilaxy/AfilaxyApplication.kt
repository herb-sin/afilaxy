package com.afilaxy

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import com.afilaxy.analytics.AnalyticsManager
import com.afilaxy.cache.SmartCache
import com.afilaxy.geofence.GeofenceManager
import com.afilaxy.notification.SmartNotificationManager
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class AfilaxyApplication : Application() {
    
    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    
    @Inject
    lateinit var analyticsManager: AnalyticsManager
    
    @Inject
    lateinit var geofenceManager: GeofenceManager
    
    @Inject
    lateinit var smartNotificationManager: SmartNotificationManager
    
    override fun onCreate() {
        super.onCreate()
        
        try {
            // Initialize analytics
            analyticsManager.initialize()
            
            // Initialize smart cache
            SmartCache.initialize()
            
            // Setup geofencing for high-risk areas
            geofenceManager.setupDefaultGeofences()
            
            // Schedule smart notifications
            smartNotificationManager.scheduleLocationReminder(this)
            
            // Register FCM token for push notifications
            GlobalScope.launch {
                com.afilaxy.notification.FCMTokenManager.updateFCMToken()
            }
        } catch (e: Exception) {
            // Log error but don't crash the app
            android.util.Log.e("AfilaxyApplication", "Initialization error", e)
        }
    }
    

}