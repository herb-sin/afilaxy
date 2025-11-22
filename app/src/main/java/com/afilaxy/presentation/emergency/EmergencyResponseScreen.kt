package com.afilaxy.presentation.emergency

import android.Manifest
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
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
import com.afilaxy.data.repository.ChatRepository
import com.afilaxy.domain.usecase.SendChatMessageUseCase
import com.afilaxy.presentation.emergency.components.EmergencyChatComponent
import com.afilaxy.security.AuthGuard
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun EmergencyResponseScreen(
    navController: NavController,
    emergencyId: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: EmergencyResponseViewModel = androidx.hilt.navigation.compose.hiltViewModel()
    
    val locationPermissions = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )
    
    // Solicitar permissões automaticamente e inicializar ViewModel
    LaunchedEffect(Unit) {
        viewModel.initialize(emergencyId, context.applicationContext as android.app.Application)
        if (!locationPermissions.allPermissionsGranted) {
            locationPermissions.launchMultiplePermissionRequest()
        }
        viewModel.loadEmergencyData()
    }
    
    // Usar localização real do usuário
    val userLocation = viewModel.helperLocation ?: LatLng(-23.5505, -46.6333)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(userLocation, 15f)
    }
    
    // Atualizar câmera quando localização mudar
    LaunchedEffect(viewModel.helperLocation) {
        viewModel.helperLocation?.let { location ->
            cameraPositionState.position = CameraPosition.fromLatLngZoom(location, 15f)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🆘 Respondendo Emergência") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    if (viewModel.canMarkAsResolved) {
                        IconButton(
                            onClick = { viewModel.markEmergencyAsResolved() }
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Marcar como resolvida",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Mapa (65% da tela)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.65f)
            ) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(
                        mapType = MapType.NORMAL,
                        isMyLocationEnabled = locationPermissions.allPermissionsGranted
                    ),
                    uiSettings = MapUiSettings(
                        zoomControlsEnabled = true,
                        compassEnabled = true,
                        myLocationButtonEnabled = true
                    )
                ) {
                    // Marcador do solicitante
                    viewModel.requesterLocation?.let { location ->
                        Marker(
                            state = MarkerState(position = location),
                            title = "Pessoa em emergência",
                            snippet = viewModel.requesterName
                        )
                    }
                    
                    // Marcador do helper (você)
                    viewModel.helperLocation?.let { location ->
                        Marker(
                            state = MarkerState(position = location),
                            title = "Sua localização",
                            snippet = "Helper"
                        )
                    }
                }
                
                // Status card sobreposto
                if (viewModel.isLoading) {
                    Card(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Carregando dados da emergência...")
                        }
                    }
                }
            }
            
            // Chat (35% da tela)
            EmergencyChatComponent(
                messages = viewModel.chatMessages,
                currentUserId = AuthGuard.getCurrentUserId() ?: "",
                onSendMessage = { message ->
                    viewModel.sendMessage(message)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.35f)
            )
        }
    }
    
    // Diálogo de confirmação para resolver emergência
    if (viewModel.showResolveDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissResolveDialog() },
            title = { Text("Emergência Resolvida?") },
            text = { 
                Text("Confirma que a emergência foi resolvida e a pessoa está bem?") 
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.confirmResolveEmergency()
                        navController.popBackStack()
                    }
                ) {
                    Text("Sim, resolvida")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissResolveDialog() }) {
                    Text("Cancelar")
                }
            }
        )
    }
}