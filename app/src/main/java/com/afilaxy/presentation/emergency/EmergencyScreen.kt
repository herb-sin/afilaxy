package com.afilaxy.presentation.emergency

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.afilaxy.domain.model.Location
import com.afilaxy.presentation.emergency.components.EmergencyInstructionsDialog
import com.afilaxy.presentation.emergency.components.BystanderInstructionsDialog
import com.afilaxy.presentation.emergency.components.HelperCard
import com.afilaxy.presentation.location.LocationViewModel
import com.afilaxy.security.AuthGuard
import com.afilaxy.ui.theme.AfilaxyTheme
import com.afilaxy.utils.GeocodingUtils
import kotlinx.coroutines.launch
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

@Composable
fun EmergencyScreen(
        navController: NavHostController,
        modifier: Modifier = Modifier,
        viewModel: EmergencyViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val locationViewModel: LocationViewModel = viewModel()
    val location by locationViewModel.location.collectAsState()
    
    var userAddress by remember { mutableStateOf<String?>(null) }

    // Inicia/paralisa atualizações de localização conforme ciclo de vida do Composable
    LaunchedEffect(Unit) { locationViewModel.startLocationUpdates(context) }
    DisposableEffect(Unit) { onDispose { locationViewModel.stopLocationUpdates(context) } }

    // Check location permission dinamicamente
    LaunchedEffect(Unit) {
        val hasLocationPermission =
                ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
        viewModel.updateLocationPermission(hasLocationPermission)
    }

    // Permission launcher
    val requestPermissionLauncher =
            rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                    onResult = { isGranted: Boolean ->
                        viewModel.updateLocationPermission(isGranted)
                        if (isGranted) {
                            fetchCurrentUserLocation(context, viewModel)
                        }
                    }
            )

    Column(
            modifier = modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
                text = "Emergência",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Spacer(modifier = Modifier.height(24.dp))

        when {
            uiState.hasLocationPermission -> {
                when {
                    uiState.isLoadingLocation -> {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Obtendo sua localização...")
                    }
                    uiState.userLocation != null -> {
                        val location = uiState.userLocation
                        
                        // Buscar endereço quando localização for obtida
                        LaunchedEffect(location) {
                            location?.let { loc ->
                                userAddress = GeocodingUtils.getAddressFromCoordinates(
                                    context,
                                    loc.latitude,
                                    loc.longitude
                                )
                            }
                        }
                        
                        Text("Sua localização foi obtida com sucesso!")
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        if (userAddress != null) {
                            Text(
                                text = userAddress!!,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                        } else {
                            Text("Obtendo endereço...")
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        when {
                            uiState.isAwaitingHelperResponse -> {
                                Text("Notificando ajudantes próximos...")
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Ajudantes encontrados:")
                                LazyColumn {
                                    items(uiState.nearbyHelpers) { helper ->
                                        HelperCard(helper = helper)
                                    }
                                }
                            }
                            uiState.helpCompleted -> {
                                Text(
                                    "✅ Ajuda finalizada!",
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("O ajudante finalizou a assistência. Esperamos que esteja bem!")
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { viewModel.resetEmergencyState() }
                                ) {
                                    Text("Nova Emergência")
                                }
                            }
                            uiState.helperResponding != null -> {
                                val helper = uiState.helperResponding
                                Text(
                                        "✅ ${helper?.nome ?: "Ajudante"} está a caminho!",
                                        color = MaterialTheme.colorScheme.primary,
                                        style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Distância: ${helper?.distanciaEstimada ?: "N/D"}")
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // Instruções de emergência na própria tela
                                androidx.compose.material3.Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = androidx.compose.material3.CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp)
                                    ) {
                                        Text(
                                            text = "🚨 Instruções de Emergência",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("• Mantenha-se calmo e respire devagar")
                                        Text("• Sente-se em posição confortável")
                                        Text("• Afrouxe roupas apertadas")
                                        Text("• Se possível, vá para local arejado")
                                        Text("• Evite esforços físicos")
                                        
                                        Spacer(modifier = Modifier.height(8.dp))
                                        
                                        Text(
                                            text = "⚠️ Se a crise piorar, chame 192 (SAMU)!",
                                            color = MaterialTheme.colorScheme.error,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Button(onClick = { viewModel.showEmergencyInstructions() }) {
                                    Text("Instruções a Transeuntes")
                                }

                                // Dialog de instruções para transeuntes
                                if (uiState.showEmergencyInstructions) {
                                    BystanderInstructionsDialog(
                                            onDismiss = { viewModel.hideEmergencyInstructions() }
                                    )
                                }
                            }
                            uiState.noHelpersFound -> {
                                Text(
                                        "Nenhum ajudante disponível no momento.",
                                        color = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(onClick = { fetchCurrentUserLocation(context, viewModel) }) {
                                    Text("Tentar Novamente")
                                }
                            }
                            else -> {
                                Text("Buscando ajuda próxima...")
                                CircularProgressIndicator()
                            }
                        }
                    }
                    uiState.locationError != null -> {
                        val error = uiState.locationError
                        Text(
                                text = error ?: "Erro desconhecido",
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { fetchCurrentUserLocation(context, viewModel) }) {
                            Text("Tentar Novamente")
                        }
                    }
                    else -> {
                        Button(
                                onClick = { 
                                    // Verificar autenticação antes de iniciar emergência
                                    if (!AuthGuard.isUserAuthenticated()) {
                                        viewModel.setLocationError("Faça login para usar o sistema de emergência")
                                        return@Button
                                    }
                                    fetchCurrentUserLocation(context, viewModel)
                                },
                                modifier = Modifier.fillMaxWidth().height(80.dp),
                                colors =
                                        ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.error
                                        )
                        ) {
                            Text(
                                    "🚨 SOS - PRECISO DE BOMBINHA",
                                    style = MaterialTheme.typography.titleLarge
                            )
                        }
                    }
                }
            }
            else -> {
                Text(
                        text =
                                "Para localizar ajuda próxima, o Afilaxy precisa da sua permissão para acessar a localização do dispositivo.",
                        textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                        onClick = {
                            requestPermissionLauncher.launch(
                                    Manifest.permission.ACCESS_FINE_LOCATION
                            )
                        }
                ) { Text("Solicitar Permissão de Localização") }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(onClick = { navController.popBackStack() }, modifier = Modifier.fillMaxWidth()) {
            Text("Voltar para Tela Inicial")
        }
    }
}

private fun fetchCurrentUserLocation(context: Context, viewModel: EmergencyViewModel) {
    if (!viewModel.uiState.value.hasLocationPermission) {
        viewModel.setLocationError("Permissão de localização não concedida.")
        return
    }

    viewModel.startLocationSearch()

    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    if (!LocationManagerCompat.isLocationEnabled(locationManager)) {
        viewModel.setLocationError(
                "Serviços de localização desativados. Por favor, ative o GPS/Localização."
        )
        return
    }

    try {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        val priority = Priority.PRIORITY_HIGH_ACCURACY
        val cancellationTokenSource = CancellationTokenSource()

        fusedLocationClient
                .getCurrentLocation(priority, cancellationTokenSource.token)
                .addOnSuccessListener { location: android.location.Location? ->
                    if (location != null) {
                        val domainLocation =
                                Location(
                                        latitude = location.latitude,
                                        longitude = location.longitude,
                                        accuracy = location.accuracy
                                )
                        viewModel.setLocation(domainLocation)
                    } else {
                        viewModel.setLocation(null)
                    }
                }
                .addOnFailureListener { exception ->
                    viewModel.setLocationError("Erro ao obter localização: ${exception.message}")
                }
    } catch (e: SecurityException) {
        viewModel.setLocationError("Erro de permissão: ${e.message}")
    }
}

@Preview(showBackground = true)
@Composable
fun EmergencyScreenPreview() {
    AfilaxyTheme { EmergencyScreen(navController = rememberNavController()) }
}
