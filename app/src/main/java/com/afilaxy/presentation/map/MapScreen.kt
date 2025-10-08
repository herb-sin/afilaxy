package com.afilaxy.presentation.map

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    navController: NavController,
    latitude: Double,
    longitude: Double,
    title: String = "Destino"
) {
    if (!com.afilaxy.security.FinalSecurityLayer.isSecureContext()) {
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
        return
    }
    
    if (!com.afilaxy.security.SecureValidator.validateCoordinates(latitude, longitude)) {
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
        return
    }
    
    val context = LocalContext.current
    val safeTitle = com.afilaxy.security.SecureValidator.validateAndSanitizeInput(title, 50).takeIf { it.isNotBlank() } ?: "Destino"
    val destination = LatLng(latitude, longitude)
    
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(destination, 15f)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Navegação - $title") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            GoogleMap(
                modifier = Modifier.weight(1f),
                cameraPositionState = cameraPositionState
            ) {
                Marker(
                    state = MarkerState(position = destination),
                    title = safeTitle,
                    snippet = "Localização da emergência"
                )
            }
            
            Button(
                onClick = {
                    val intent = android.content.Intent(
                        android.content.Intent.ACTION_VIEW,
                        android.net.Uri.parse("google.navigation:q=$latitude,$longitude")
                    )
                    intent.setPackage("com.google.android.apps.maps")
                    com.afilaxy.security.FinalSecurityLayer.secureOperation("openMaps") {
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            val browserIntent = android.content.Intent(
                                android.content.Intent.ACTION_VIEW,
                                android.net.Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$latitude,$longitude")
                            )
                            context.startActivity(browserIntent)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("🗺️ Abrir no Google Maps")
            }
        }
    }
}