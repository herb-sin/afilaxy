package com.afilaxy.presentation.helper

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.afilaxy.ui.theme.AfilaxyTheme

@Composable
fun HelperResponseScreen(
    navController: NavHostController,
    emergencyId: String? = null,
    modifier: Modifier = Modifier,
    viewModel: HelperResponseViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    LaunchedEffect(emergencyId) {
        try {
            emergencyId?.let { 
                if (com.afilaxy.security.CentralizedValidator.validateInput(it, com.afilaxy.security.CentralizedValidator.InputType.GENERAL).isValid) {
                    viewModel.loadEmergency(it)
                } else {
                    com.afilaxy.security.SecurityMonitor.reportSecurityEvent("INVALID_EMERGENCY_ID", "Invalid emergency ID format")
                }
            }
        } catch (e: Exception) {
            com.afilaxy.security.SecureLogger.e("HelperResponseScreen", "Error loading emergency", e)
        }
    }
    
    // Função para abrir navegação usando remember
    val openNavigation = remember {
        { lat: Double, lon: Double ->
            try {
                // Validate coordinates before creating URI to prevent XXE
                if (!com.afilaxy.security.SecurityValidator.validateCoordinates(lat, lon)) {
                    com.afilaxy.security.SecurityMonitor.reportSecurityEvent("NAVIGATION_XXE", "Invalid coordinates")
                    return@remember
                }
                
                val gmmIntentUri = Uri.parse("google.navigation:q=$lat,$lon")
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")
                context.startActivity(mapIntent)
            } catch (e: Exception) {
                try {
                    val browserUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$lat,$lon")
                    val browserIntent = Intent(Intent.ACTION_VIEW, browserUri)
                    context.startActivity(browserIntent)
                } catch (e2: Exception) {
                    com.afilaxy.security.SecureLogger.e("Navigation", "Navigation error", e2)
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🚨 EMERGÊNCIA",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                when {
                    uiState.isLoading -> {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Carregando detalhes...")
                    }
                    
                    uiState.emergency != null -> {
                        val emergency = uiState.emergency!!
                        Text(
                            text = "${emergency.userName} precisa de bombinha!",
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Distância: ${uiState.distance ?: "Calculando..."}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Há ${uiState.timeAgo}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    else -> {
                        Text(
                            text = "Nenhuma emergência ativa",
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Aguardando pedidos de ajuda...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        when {
            uiState.isAccepting -> {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(8.dp))
                Text("Confirmando sua ajuda...")
            }
            
            uiState.hasAccepted -> {
                Text(
                    text = "✅ Você aceitou ajudar!",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFF4CAF50)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "A pessoa foi notificada que você está a caminho.",
                    textAlign = TextAlign.Center
                )
                
                // Mostrar localização de destino
                uiState.emergency?.let { emergency ->
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "📍 Localização de Destino",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Lat: ${String.format("%.6f", emergency.location.latitude)}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Lon: ${String.format("%.6f", emergency.location.longitude)}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Distância: ${uiState.distance ?: "N/D"}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Botões para navegação
                uiState.emergency?.let { emergency ->
                    Button(
                        onClick = { 
                            openNavigation(emergency.location.latitude, emergency.location.longitude)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("🗺️ Abrir Navegação")
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedButton(
                        onClick = { 
                            viewModel.finishHelp()
                            navController.popBackStack() 
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Finalizar Ajuda")
                    }
                } ?: run {
                    Button(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Voltar")
                    }
                }
            }
            
            else -> {
                Button(
                    onClick = { 
                        viewModel.acceptEmergency()
                        // Navegar para tela de navegação integrada
                        uiState.emergency?.let { emergency ->
                            navController.navigate(
                                "navigation/${emergency.location.latitude}/${emergency.location.longitude}/Pessoa"
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    Text(
                        "✋ ACEITAR E NAVEGAR",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Não posso ajudar agora")
                }
            }
        }

        uiState.error?.let { error ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HelperResponseScreenPreview() {
    AfilaxyTheme {
        HelperResponseScreen(navController = rememberNavController())
    }
}