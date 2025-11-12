package com.afilaxy.presentation.map

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    // Localização padrão com tratamento de erro
    val defaultLocation = try {
        LatLng(-23.5505, -46.6333) // São Paulo como fallback seguro
    } catch (e: Exception) {
        android.util.Log.e("MapScreen", "Erro ao criar localização padrão", e)
        LatLng(0.0, 0.0)
    }
    
    val cameraPositionState = rememberCameraPositionState {
        try {
            position = CameraPosition.fromLatLngZoom(defaultLocation, 12f)
        } catch (e: Exception) {
            android.util.Log.e("MapScreen", "Erro ao inicializar câmera do mapa", e)
            position = CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 1f)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // TopAppBar
        TopAppBar(
            title = { Text("Mapa Afilaxy") },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                }
            }
        )
        
        // Mapa
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = false,
                mapType = MapType.NORMAL
            ),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = true,
                myLocationButtonEnabled = false
            )
        ) {
            // Marcador de exemplo
            Marker(
                state = MarkerState(position = defaultLocation),
                title = "Afilaxy",
                snippet = "Localização de exemplo"
            )
        }
    }
}