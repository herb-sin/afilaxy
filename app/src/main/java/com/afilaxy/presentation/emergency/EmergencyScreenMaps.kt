package com.afilaxy.presentation.emergency

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.afilaxy.location.LocationPermissionHandler
import com.afilaxy.performance.AnrOptimizer
import com.afilaxy.performance.MapsOptimizer
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyScreenMaps(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val viewModel: EmergencyViewModel = viewModel { EmergencyViewModel(context) }
    var hasLocationPermission by remember { mutableStateOf(false) }

    LocationPermissionHandler(
        onPermissionGranted = { 
            hasLocationPermission = true
            viewModel.refreshLocation()
        },
        onPermissionDenied = { 
            hasLocationPermission = false 
        }
    ) {
        EmergencyMapContent(
            viewModel = viewModel,
            navController = navController,
            hasLocationPermission = hasLocationPermission,
            modifier = modifier
        )
    }
}

@Composable
private fun EmergencyMapContent(
    viewModel: EmergencyViewModel,
    navController: NavController,
    hasLocationPermission: Boolean,
    modifier: Modifier = Modifier
) {
    // Localização do usuário (GPS ou padrão)
    val userLatLng = remember(viewModel.userLocation) {
        if (viewModel.userLocation.contains(",")) {
            val coords = viewModel.userLocation.split(",")
            try {
                val lat = coords[0].trim().toDouble()
                val lng = coords[1].trim().toDouble()
                LatLng(lat, lng)
            } catch (e: Exception) {
                LatLng(-23.5505, -46.6333) // Fallback
            }
        } else {
            LatLng(-23.5505, -46.6333) // Fallback
        }
    }
    
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(userLatLng, 15f)
    }

    // Update camera when location changes
    LaunchedEffect(userLatLng) {
        try {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(userLatLng, 15f)
            )
        } catch (e: Exception) {
            // Ignore camera errors
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Google Maps
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
            // User location marker
            Marker(
                state = MarkerState(position = userLatLng),
                title = "Sua localização",
                snippet = "Você está aqui"
            )
            
            // Helper markers reais do Firebase
            if (viewModel.emergencyActive && viewModel.nearbyHelpers.isNotEmpty()) {
                viewModel.nearbyHelpers.forEach { helper ->
                    Marker(
                        state = MarkerState(position = LatLng(helper.latitude, helper.longitude)),
                        title = helper.name,
                        snippet = "Bombinha disponível - ${String.format("%.0f", helper.distance * 1000)}m"
                    )
                }
            }
        }
        
        // Top buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopStart),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = { navController.popBackStack() }
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Voltar",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            if (hasLocationPermission) {
                IconButton(
                    onClick = { viewModel.refreshLocation() }
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Atualizar Localização",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        
        // Emergency controls card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "🆘 Emergência",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                // Status message
                if (viewModel.statusMessage.isNotEmpty()) {
                    Text(
                        viewModel.statusMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                if (!viewModel.emergencyActive) {
                    Text(
                        "Precisa de uma bombinha de asma?",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Button(
                        onClick = { viewModel.requestHelp() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        enabled = !viewModel.isLoading
                    ) {
                        if (viewModel.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onError
                            )
                        } else {
                            Text("SOLICITAR AJUDA")
                        }
                    }
                } else {
                    Text(
                        "✅ Ajuda solicitada!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (viewModel.helpersFound > 0) {
                        Text(
                            "${viewModel.helpersFound} helpers encontrados no mapa",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        Text(
                            "Nenhum helper próximo encontrado",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Button(
                        onClick = { viewModel.cancelHelp() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Text("Cancelar Solicitação")
                    }
                }
            }
        }
    }
}