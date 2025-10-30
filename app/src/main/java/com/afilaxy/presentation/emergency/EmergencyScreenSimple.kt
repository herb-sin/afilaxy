package com.afilaxy.presentation.emergency

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.afilaxy.location.LocationPermissionHandler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyScreenSimple(
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
        EmergencyContent(
            viewModel = viewModel,
            navController = navController,
            hasLocationPermission = hasLocationPermission,
            modifier = modifier
        )
    }
}

@Composable
private fun EmergencyContent(
    viewModel: EmergencyViewModel,
    navController: NavController,
    hasLocationPermission: Boolean,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        // Mapa simulado
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF4CAF50)), // Verde simulando mapa
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "🗺️ MAPA SIMULADO",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "São Paulo, SP",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                // Marcadores simulados
                if (viewModel.emergencyActive) {
                    Text(
                        "📍 Você está aqui",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "🏥 Helper 1 - 200m",
                        color = Color.Blue,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "🏥 Helper 2 - 350m", 
                        color = Color.Blue,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "🏥 Helper 3 - 500m",
                        color = Color.Blue,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        
        // Botões superiores
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
                    tint = Color.White
                )
            }
            
            if (hasLocationPermission) {
                IconButton(
                    onClick = { viewModel.refreshLocation() }
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Atualizar Localização",
                        tint = Color.White
                    )
                }
            }
        }
        
        // Card de controles
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
                
                // Status Message
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