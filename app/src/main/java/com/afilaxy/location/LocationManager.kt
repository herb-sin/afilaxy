package com.afilaxy.location

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.location.LocationCallback
import com.afilaxy.startSignificantMovementUpdates
import com.afilaxy.saveUserLocationWithCoords

class LocationManager {
    
    @Composable
    fun SetupLocationPermissions(
        onLocationCallback: (LocationCallback?) -> Unit,
        onLocationUpdatesActive: (Boolean) -> Unit
    ) {
        val context = LocalContext.current
        var isLocationUpdatesActive by remember { mutableStateOf(false) }
        
        val requestLocationPermissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { isGranted: Boolean ->
                if (isGranted && !isLocationUpdatesActive) {
                    try {
                        val callback = startSignificantMovementUpdates(
                            context,
                            minDistanceMeters = 50f
                        ) { lat: Double, lon: Double ->
                            try {
                                saveUserLocationWithCoords(context, lat, lon)
                            } catch (e: Exception) {
                                android.util.Log.e("LocationManager", "Erro ao salvar localização: ${e.message}")
                            }
                        }
                        onLocationCallback(callback)
                        isLocationUpdatesActive = true
                        onLocationUpdatesActive(true)
                    } catch (e: Exception) {
                        android.util.Log.e("LocationManager", "Erro ao iniciar atualizações: ${e.message}")
                    }
                }
            }
        )
        
        LaunchedEffect(Unit) {
            requestLocationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
}