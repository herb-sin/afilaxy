package com.afilaxy.location

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.*

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationPermissionHandler(
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit,
    content: @Composable () -> Unit
) {
    val locationPermissions = rememberMultiplePermissionsState(
        permissions = listOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    LaunchedEffect(locationPermissions.allPermissionsGranted) {
        if (locationPermissions.allPermissionsGranted) {
            onPermissionGranted()
        }
    }

    when {
        locationPermissions.allPermissionsGranted -> {
            content()
        }
        locationPermissions.shouldShowRationale -> {
            LocationPermissionRationale(
                onRequestPermission = { locationPermissions.launchMultiplePermissionRequest() },
                onDismiss = onPermissionDenied
            )
        }
        else -> {
            LaunchedEffect(Unit) {
                locationPermissions.launchMultiplePermissionRequest()
            }
            content()
        }
    }
}

@Composable
private fun LocationPermissionRationale(
    onRequestPermission: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Permissão de Localização") },
        text = { 
            Text("O Afilaxy precisa acessar sua localização para encontrar helpers próximos em emergências de asma.")
        },
        confirmButton = {
            TextButton(onClick = onRequestPermission) {
                Text("Permitir")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}