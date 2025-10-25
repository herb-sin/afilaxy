package com.afilaxy

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.Composable
import androidx.core.app.ActivityCompat
import androidx.navigation.compose.rememberNavController
import com.afilaxy.presentation.common.navigation.AppNavigation
import com.afilaxy.ui.theme.AfilaxyTheme
import com.afilaxy.ui.RequestNotificationPermission

// import com.afilaxy.notification.NotificationManager
import com.afilaxy.security.AuthGuard
import com.afilaxy.security.InputSanitizer
import com.afilaxy.security.SecureLogger
import com.afilaxy.security.SqlInjectionPrevention
import com.afilaxy.security.ErrorHandler
import com.afilaxy.stopLocationUpdates
import com.google.android.gms.location.LocationCallback
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main activity for Afilaxy application
 * Handles navigation, permissions, and security initialization
 * 
 * Security features:
 * - Authentication checks
 * - Input sanitization for intents
 * - Secure error handling
 * - Permission validation
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var locationCallback: LocationCallback? = null
    // private val notificationManager = NotificationManager()
    private val firebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            if (!AuthGuard.isUserAuthenticated()) {
                SecureLogger.w("MainActivity", "User not authenticated - allowing access to login")
                // Allow access for login screen
            }
            
            initializeFirebase()
            setupLocationPermissions()
            
            setContent {
                MainContent()
            }
            
        } catch (e: Exception) {
            val error = ErrorHandler.handleException(e, "MAIN_ACTIVITY_INIT")
            SecureLogger.e("MainActivity", "Initialization error", e)
            // Continue with basic functionality even if some features fail
            setContent {
                MainContent()
            }
        }
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        SecureLogger.d("MainActivity", "onNewIntent called with intent data")
    }

    @Composable
    private fun MainContent() {
        AfilaxyTheme {
            val navController = rememberNavController()
            
            SetupAuthListener()
            SetupNotificationListener(navController)
            HandleEmergencyIntent(navController)
            
            val (callback, isActive) = SetupLocationManager()
            RequestNotificationPermission()
            
            Surface(modifier = Modifier.fillMaxSize()) {
                AppNavigation(
                    navController = navController,
                    modifier = Modifier.fillMaxSize(),
                    onLocationCallbackUpdate = { newCallback ->
                        locationCallback = newCallback as? LocationCallback
                    }
                )
            }
            
            // Cleanup resources
            CleanupResources(isActive, callback)
        }
    }

    @Composable
    private fun SetupAuthListener() {
        LaunchedEffect(Unit) {
            try {
                firebaseAuth.addAuthStateListener { auth ->
                    if (auth.currentUser == null) {
                        SecureLogger.security("AUTH_STATE_CHANGE", "USER_LOGGED_OUT")
                        // Handle logout - clear sensitive data
                        locationCallback = null
                        // notificationManager.cleanup()
                    } else {
                        SecureLogger.security("AUTH_STATE_CHANGE", "USER_LOGGED_IN")
                    }
                }
            } catch (e: Exception) {
                val error = ErrorHandler.handleException(e, "SETUP_AUTH_LISTENER")
                SecureLogger.e("MainActivity", "Error setting up auth listener", e)
            }
        }
    }

    @Composable
    private fun SetupNotificationListener(navController: androidx.navigation.NavController) {
        LaunchedEffect(Unit) {
            try {
                // notificationManager.setupNotificationListener(navController)
            } catch (e: SecurityException) {
                SecureLogger.security("NOTIFICATION_SETUP", "PERMISSION_DENIED")
                SecureLogger.e("MainActivity", "Notification permission denied", e)
            } catch (e: Exception) {
                val error = ErrorHandler.handleException(e, "SETUP_NOTIFICATIONS")
                SecureLogger.e("MainActivity", "Error setting up notifications", e)
            }
        }
    }

    @Composable
    private fun HandleEmergencyIntent(navController: androidx.navigation.NavController) {
        LaunchedEffect(intent) {
            try {
                SecureLogger.d("MainActivity", "Checking intent for emergency data")
                
                val rawEmergencyId = intent.getStringExtra("emergency_id") ?: ""
                val rawRequesterName = intent.getStringExtra("requester_name") ?: ""
                val rawNotificationType = intent.getStringExtra("notification_type") ?: ""
                
                // Validate inputs for injection attacks
                val emergencyId = if (SqlInjectionPrevention.isValidSqlInput(rawEmergencyId)) {
                    InputSanitizer.sanitizeText(rawEmergencyId)
                } else {
                    SecureLogger.security("INTENT_VALIDATION", "INVALID_EMERGENCY_ID")
                    ""
                }
                
                val requesterName = if (SqlInjectionPrevention.isValidSqlInput(rawRequesterName)) {
                    InputSanitizer.sanitizeName(rawRequesterName)
                } else {
                    SecureLogger.security("INTENT_VALIDATION", "INVALID_REQUESTER_NAME")
                    ""
                }
                
                val notificationType = if (SqlInjectionPrevention.isValidSqlInput(rawNotificationType)) {
                    InputSanitizer.sanitizeText(rawNotificationType)
                } else {
                    SecureLogger.security("INTENT_VALIDATION", "INVALID_NOTIFICATION_TYPE")
                    ""
                }
                
                SecureLogger.d("MainActivity", "Intent data validated")
                SecureLogger.d("MainActivity", "Emergency ID present: ${emergencyId.isNotBlank()}")
                SecureLogger.d("MainActivity", "Requester present: ${requesterName.isNotBlank()}")
                SecureLogger.d("MainActivity", "Type: $notificationType")
                
                if (notificationType == "emergency_alert" && emergencyId.isNotBlank() && requesterName.isNotBlank()) {
                    try {
                        SecureLogger.d("MainActivity", "Navigating to emergency response")
                        navController.navigate("emergency_response/$emergencyId/$requesterName")
                    } catch (e: Exception) {
                        val error = ErrorHandler.handleException(e, "NAVIGATE_EMERGENCY")
                        SecureLogger.e("MainActivity", "Navigation error", e)
                    }
                } else {
                    SecureLogger.w("MainActivity", "Intent is not emergency or missing data")
                }
                
            } catch (e: Exception) {
                val error = ErrorHandler.handleException(e, "HANDLE_EMERGENCY_INTENT")
                SecureLogger.e("MainActivity", "Error handling emergency intent", e)
            }
        }
    }

    @Composable
    private fun SetupLocationManager(): Pair<LocationCallback?, Boolean> {
        var isLocationUpdatesActive by remember { mutableStateOf(false) }
        var currentCallback by remember { mutableStateOf<LocationCallback?>(null) }
        
        // Lazy initialization for better performance - removed unused locationManager
        
        LaunchedEffect(Unit) {
            try {
                // Only initialize if user is authenticated
                if (AuthGuard.isUserAuthenticated()) {
                    isLocationUpdatesActive = true
                } else {
                    SecureLogger.w("MainActivity", "Location updates require authentication")
                }
            } catch (e: Exception) {
                SecureLogger.e("MainActivity", "Location setup error", e)
            }
        }
        
        return Pair(currentCallback, isLocationUpdatesActive)
    }



    @Composable
    private fun CleanupResources(isLocationActive: Boolean, callback: LocationCallback?) {
        val context = LocalContext.current
        DisposableEffect(isLocationActive) {
            onDispose {
                try {
                    callback?.let { stopLocationUpdates(context, it) }
                    // notificationManager.cleanup()
                } catch (e: SecurityException) {
                    SecureLogger.security("CLEANUP", "PERMISSION_ERROR")
                    SecureLogger.e("MainActivity", "Permission error during cleanup", e)
                } catch (e: Exception) {
                    val error = ErrorHandler.handleException(e, "CLEANUP_RESOURCES")
                    SecureLogger.e("MainActivity", "Cleanup error", e)
                }
            }
        }
    }
    
    private fun initializeFirebase() {
        try {
            // Use secure Firebase configuration
            com.afilaxy.config.FirebaseConfig.initializeFromEnvironment(this)
            
            val auth = firebaseAuth
            
            // Configure Firebase Auth for development
            if (BuildConfig.DEBUG) {
                android.util.Log.d("MainActivity", "Firebase Auth configurado para desenvolvimento")
            }
            
            val isEmulator = android.os.Build.FINGERPRINT.contains("generic") ||
                           android.os.Build.MODEL.contains("Emulator") ||
                           android.os.Build.MANUFACTURER.contains("Genymotion")
            
            if (isEmulator) {
                try {
                    auth.useEmulator("10.0.2.2", 9099)
                } catch (e: Exception) {
                    android.util.Log.w("MainActivity", "Emulator config failed", e)
                }
            }

        } catch (e: Exception) {
            val error = ErrorHandler.handleFirebaseError(e, "FIREBASE_INIT")
            SecureLogger.e("MainActivity", "Firebase initialization failed", e)
        }
    }
    
    private fun setupLocationPermissions() {
        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                requestBackgroundLocationPermission()
            }
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
    
    private fun requestBackgroundLocationPermission() {
        if (!AuthGuard.isUserAuthenticated()) {
            SecureLogger.security("LOCATION_PERMISSION", "UNAUTHENTICATED_REQUEST")
            return
        }
        
        try {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                1001
            )
        } catch (e: SecurityException) {
            SecureLogger.security("LOCATION_PERMISSION", "PERMISSION_DENIED")
            SecureLogger.e("MainActivity", "Location permission denied", e)
        } catch (e: Exception) {
            val error = ErrorHandler.handleException(e, "REQUEST_LOCATION_PERMISSION")
            SecureLogger.e("MainActivity", "Error requesting location permission", e)
        }
    }
}