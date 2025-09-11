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
import androidx.core.app.ActivityCompat
import androidx.navigation.compose.rememberNavController
import com.afilaxy.presentation.common.navigation.AppNavigation
import com.afilaxy.ui.theme.AfilaxyTheme
import com.afilaxy.ui.RequestNotificationPermission
import com.google.android.gms.location.LocationCallback
import com.google.firebase.FirebaseApp


class MainActivity : ComponentActivity() {
    private var locationCallback: LocationCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        // Solicita permissão de localização
        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                    1001
                )
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        setContent {
            RequestNotificationPermission()
            AfilaxyTheme {
                val navController = rememberNavController()
                val context = LocalContext.current
                var isLocationUpdatesActive by remember { mutableStateOf(false) }

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    val requestPermissionLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestPermission(),
                        onResult = { /* handle notification permission result */ }
                    )
                    LaunchedEffect(Unit) {
                        requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                    }
                }

                // Launcher para solicitar permissão de localização
                val requestLocationPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                    onResult = { isGranted: Boolean ->
                        if (isGranted) {
                            // Inicia atualização contínua de localização
                            if (!isLocationUpdatesActive) {
                                locationCallback = startSignificantMovementUpdates(
                                    context,
                                    minDistanceMeters = 50f
                                ) { lat, lon ->
                                    saveUserLocationWithCoords(context, lat, lon)
                                }
                                isLocationUpdatesActive = true
                            }
                        }
                    }
                )

                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    AppNavigation(
                        navController = navController,
                        modifier = Modifier.fillMaxSize(),
                        onLocationCallbackUpdate = { callback ->
                            locationCallback = callback as? LocationCallback
                        }
                    )
                }

                // Cleanup location updates
                DisposableEffect(isLocationUpdatesActive) {
                    onDispose {
                        locationCallback?.let {
                            stopLocationUpdates(context, it)
                        }
                        isLocationUpdatesActive = false
                    }
                }
            }
        }
    }
}