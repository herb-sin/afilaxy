package com.afilaxy.presentation.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.afilaxy.security.SecureLogger

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationScreen(
    destinationLat: Double,
    destinationLng: Double,
    destinationName: String = "Pessoa em emergência",
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current
    val destination = LatLng(destinationLat, destinationLng)
    
    val cameraPositionState = rememberCameraPositionState {
        position = com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(destination, 15f)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Navegação para Pessoa") },
            navigationIcon = {
                IconButton(onClick = onBackPressed) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                }
            }
        )
        
        GoogleMap(
            modifier = Modifier.weight(1f),
            cameraPositionState = cameraPositionState
        ) {
            Marker(
                state = MarkerState(position = destination),
                title = "Pessoa em emergência"
            )
        }
        
        Button(
            onClick = {
                try {
                    // Validar coordenadas para prevenir XXE
                    val safeLat = destinationLat.toString().replace("[^0-9.-]".toRegex(), "")
                    val safeLng = destinationLng.toString().replace("[^0-9.-]".toRegex(), "")
                    val uri = "google.navigation:q=$safeLat,$safeLng"
                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(uri))
                    context.startActivity(intent)
                } catch (e: Exception) {
                    SecureLogger.e("NavigationScreen", "Erro ao iniciar navegação", e)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Iniciar Navegação Externa")
        }
    }
}