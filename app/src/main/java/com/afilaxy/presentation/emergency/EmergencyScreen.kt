package com.afilaxy.presentation.emergency

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val viewModel: EmergencyViewModel = viewModel()
    
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
            cameraPositionState = cameraPositionState
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
                        "${viewModel.helpersFound} pessoas próximas notificadas",
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