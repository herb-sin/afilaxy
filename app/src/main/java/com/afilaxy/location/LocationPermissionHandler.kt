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
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
        )
    )
    
    var showBackgroundLocationDialog by remember { mutableStateOf(false) }

    LaunchedEffect(locationPermissions.allPermissionsGranted) {
        if (locationPermissions.allPermissionsGranted) {
            onPermissionGranted()
        }
    }
    
    // Verificar se precisa mostrar aviso sobre background location
    val basicLocationGranted = locationPermissions.permissions.take(2).all { it.status == PermissionStatus.Granted }
    val backgroundLocationGranted = locationPermissions.permissions.getOrNull(2)?.status == PermissionStatus.Granted
    
    LaunchedEffect(basicLocationGranted, backgroundLocationGranted) {
        if (basicLocationGranted && !backgroundLocationGranted) {
            showBackgroundLocationDialog = true
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
    
    // Dialog para orientar sobre "Permitir o tempo todo"
    if (showBackgroundLocationDialog) {
        BackgroundLocationDialog(
            onDismiss = { showBackgroundLocationDialog = false },
            onRequestPermission = {
                showBackgroundLocationDialog = false
                locationPermissions.launchMultiplePermissionRequest()
            }
        )
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

@Composable
private fun BackgroundLocationDialog(
    onDismiss: () -> Unit,
    onRequestPermission: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("⚠️ Importante para Emergências") },
        text = { 
            Text(
                "Para receber alertas de emergência mesmo com o app fechado, " +
                "selecione 'Permitir o tempo todo' na próxima tela.\n\n" +
                "Isso é essencial para salvar vidas em crises de asma!"
            )
        },
        confirmButton = {
            TextButton(onClick = onRequestPermission) {
                Text("Entendi, Continuar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Agora Não")
            }
        }
    )
}