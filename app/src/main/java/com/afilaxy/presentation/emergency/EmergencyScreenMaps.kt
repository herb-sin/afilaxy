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
    val viewModel: EmergencyViewModel = viewModel()
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
    // Parse user location with validation
    val userLatLng = remember(viewModel.userLocation) {
        if (viewModel.userLocation.contains(",")) {
            val coords = viewModel.userLocation.split(",")
            try {
                val lat = coords[0].trim().toDouble()
                val lng = coords[1].trim().toDouble()
                MapsOptimizer.validateCoordinates(lat, lng)
            } catch (e: Exception) {
                LatLng(-23.5505, -46.6333) // São Paulo fallback
            }
        } else {
            LatLng(-23.5505, -46.6333) // São Paulo fallback
        }
    }
    
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(userLatLng, 15f)
    }

    // Update camera when location changes with optimized delay
    LaunchedEffect(userLatLng) {
        MapsOptimizer.safeCameraOperation {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(userLatLng, 15f)
            )
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Google Maps with optimized settings
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapsOptimizer.getOptimizedMapProperties(hasLocationPermission),
            uiSettings = MapsOptimizer.getOptimizedMapUiSettings()
        ) {
            // User location marker
            Marker(
                state = MarkerState(position = userLatLng),
                title = "Sua localização",
                snippet = "Você está aqui"
            )
            
            // Helper markers when emergency is active (optimized)
            if (viewModel.emergencyActive) {
                val helpers = remember(userLatLng) {
                    MapsOptimizer.generateOptimizedHelpers(userLatLng, viewModel.helpersFound)
                }
                
                helpers.forEachIndexed { index, location ->
                    Marker(
                        state = MarkerState(position = location),
                        title = "Helper ${index + 1}",
                        snippet = "Bombinha disponível - ${(100..500).random()}m"
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
                    Text(
                        "${viewModel.helpersFound} helpers próximos no mapa",
                        style = MaterialTheme.typography.bodyMedium
                    )
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