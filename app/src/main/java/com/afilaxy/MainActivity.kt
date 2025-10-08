package com.afilaxy

import android.Manifest
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

import com.afilaxy.notification.NotificationManager
import com.afilaxy.security.AuthGuard
import com.afilaxy.stopLocationUpdates
import com.google.android.gms.location.LocationCallback
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var locationCallback: LocationCallback? = null
    private val notificationManager = NotificationManager()
    private val firebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        if (!AuthGuard.isUserAuthenticated()) {
            android.util.Log.w("MainActivity", "Usuário não autenticado")
        }
        
        try {
            initializeFirebase()
            setupLocationPermissions()
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Erro na inicialização: ${e.message}")
            return
        }
        
        setContent {
            MainContent()
        }
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
            firebaseAuth.addAuthStateListener { auth ->
                if (auth.currentUser == null) {
                    // Handle logout - clear sensitive data
                    locationCallback = null
                    notificationManager.cleanup()
                }
            }
        }
    }

    @Composable
    private fun SetupNotificationListener(navController: androidx.navigation.NavController) {
        LaunchedEffect(Unit) {
            try {
                notificationManager.setupNotificationListener(navController)
            } catch (e: SecurityException) {
                android.util.Log.e("MainActivity", "Permissão negada para notificações: ${e.message}")
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Erro ao configurar notificações: ${e.message}")
            }
        }
    }

    @Composable
    private fun HandleEmergencyIntent(navController: androidx.navigation.NavController) {
        LaunchedEffect(Unit) {
            if (intent.getBooleanExtra("open_emergency", false)) {
                try {
                    navController.navigate("tela_helper_response")
                } catch (e: IllegalArgumentException) {
                    android.util.Log.e("MainActivity", "Rota de emergência inválida: ${e.message}")
                } catch (e: Exception) {
                    android.util.Log.e("MainActivity", "Erro ao navegar para emergência: ${e.message}")
                }
            }
        }
    }

    @Composable
    private fun SetupLocationManager(): Pair<LocationCallback?, Boolean> {
        var isLocationUpdatesActive by remember { mutableStateOf(false) }
        var currentCallback by remember { mutableStateOf<LocationCallback?>(null) }
        
        val locationManager = remember { com.afilaxy.location.LocationManager() }
        
        LaunchedEffect(Unit) {
            // Simplified location setup
            isLocationUpdatesActive = true
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
                    notificationManager.cleanup()
                } catch (e: SecurityException) {
                    android.util.Log.e("MainActivity", "Erro de permissão no cleanup: ${e.message}")
                } catch (e: Exception) {
                    android.util.Log.e("MainActivity", "Erro no cleanup: ${e.message}")
                }
            }
        }
    }
    
    private fun initializeFirebase() {
        try {
            // Use secure Firebase configuration
            com.afilaxy.config.FirebaseConfig.initializeFromEnvironment(this)
            
            val auth = firebaseAuth
            
            val isEmulator = android.os.Build.FINGERPRINT.contains("generic") ||
                           android.os.Build.MODEL.contains("Emulator") ||
                           android.os.Build.MANUFACTURER.contains("Genymotion")
            
            if (isEmulator) {
                try {
                    auth.useEmulator("10.0.2.2", 9099)
                } catch (e: Exception) {
                    com.afilaxy.security.SecurityUtils.safeLog("MainActivity", "Emulator config failed: ${e.message}", com.afilaxy.security.SecurityUtils.LogLevel.WARN)
                }
            }

        } catch (e: Exception) {
            com.afilaxy.security.SecurityUtils.safeLog("MainActivity", "Firebase initialization failed: ${e.message}", com.afilaxy.security.SecurityUtils.LogLevel.ERROR)
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
        try {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                1001
            )
        } catch (e: SecurityException) {
            android.util.Log.e("MainActivity", "Permissão de localização negada: ${e.message}")
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Erro ao solicitar permissão: ${e.message}")
        }
    }
}