package com.afilaxy.presentation.emergency

import android.Manifest
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun EmergencyScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: EmergencyViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return EmergencyViewModel(context) as T
            }
        }
    )
    
    // Permissões de localização
    val locationPermissions = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )
    
    // Solicitar permissões automaticamente
    LaunchedEffect(Unit) {
        if (!locationPermissions.allPermissionsGranted) {
            locationPermissions.launchMultiplePermissionRequest()
        }
    }
    
    // Localização padrão (São Paulo) caso não tenha GPS
    val defaultLocation = LatLng(-23.5505, -46.6333)
    val userLatLng = remember(viewModel.userLocation) {
        if (viewModel.userLocation.contains(",")) {
            val coords = viewModel.userLocation.split(",")
            try {
                LatLng(coords[0].trim().toDouble(), coords[1].trim().toDouble())
            } catch (e: Exception) {
                defaultLocation
            }
        } else {
            defaultLocation
        }
    }
    
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(userLatLng, 15f)
    }
    
    Box(modifier = modifier.fillMaxSize()) {
        // Mapa ocupando toda a tela
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                mapType = MapType.NORMAL
            ),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = true,
                compassEnabled = true
            )
        ) {
            // Marcador da localização do usuário
            Marker(
                state = MarkerState(position = userLatLng),
                title = "Sua localização",
                snippet = "Você está aqui"
            )
            
            // Marcadores de helpers próximos (mock data)
            if (viewModel.emergencyActive) {
                val helpers = listOf(
                    LatLng(userLatLng.latitude + 0.001, userLatLng.longitude + 0.001),
                    LatLng(userLatLng.latitude - 0.002, userLatLng.longitude + 0.003),
                    LatLng(userLatLng.latitude + 0.003, userLatLng.longitude - 0.001)
                )
                
                helpers.forEachIndexed { index, location ->
                    Marker(
                        state = MarkerState(position = location),
                        title = "Helper ${index + 1}",
                        snippet = "Bombinha disponível"
                    )
                }
            }
        }
        
        // Botão de voltar
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Voltar",
                tint = MaterialTheme.colorScheme.primary
            )
        }
        
        // Card de controles na parte inferior
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
                
                if (!viewModel.emergencyActive) {
                    if (!locationPermissions.allPermissionsGranted) {
                        Text(
                            "📍 Permissão de localização necessária",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { locationPermissions.launchMultiplePermissionRequest() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Permitir Localização")
                        }
                    } else {
                        Text(
                            "Precisa de uma bombinha de asma?",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            viewModel.statusMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    if (locationPermissions.allPermissionsGranted) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.refreshLocation() },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary
                                )
                            ) {
                                Icon(Icons.Default.LocationOn, contentDescription = null)
                            }
                            Button(
                                onClick = { viewModel.requestHelp() },
                                modifier = Modifier.weight(3f),
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
                        "${viewModel.helpersFound} pessoas próximas notificadas",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        viewModel.statusMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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