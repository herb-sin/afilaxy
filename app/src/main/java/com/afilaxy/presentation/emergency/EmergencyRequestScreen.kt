package com.afilaxy.presentation.emergency

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.afilaxy.presentation.emergency.components.EmergencyChatComponent
import com.afilaxy.security.AuthGuard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyRequestScreen(
    navController: NavController,
    emergencyId: String,
    modifier: Modifier = Modifier,
    viewModel: EmergencyRequestViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    
    // Monitorar status da emergência
    LaunchedEffect(emergencyId) {
        viewModel.monitorEmergencyStatus(emergencyId)
    }
    
    // Navegar para chat quando aceito
    LaunchedEffect(state.isAccepted) {
        if (state.isAccepted && state.helperName != null) {
            navController.navigate("emergency_response/$emergencyId/${state.helperName}")
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🆘 Aguardando Ajuda") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    when (state.status) {
                        "pending" -> {
                            Text(
                                "⏳ Aguardando ajuda...",
                                style = MaterialTheme.typography.headlineSmall
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Pessoas próximas foram notificadas. Aguarde alguém aceitar sua solicitação.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        "accepted" -> {
                            Text(
                                "✅ Ajuda encontrada!",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "${state.helperName ?: "Alguém"} aceitou ajudar você. Redirecionando para o chat...",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        else -> {
                            Text(
                                "📱 Pedido de ajuda enviado!",
                                style = MaterialTheme.typography.headlineSmall
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Status: ${state.status}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    
                    state.error?.let { error ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "⚠️ $error",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                "💬 Chat de Emergência",
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Chat component
            EmergencyChatComponent(
                messages = emptyList(), // TODO: Implementar ViewModel
                currentUserId = AuthGuard.getCurrentUserId() ?: "",
                onSendMessage = { message ->
                    // TODO: Implementar envio de mensagem
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}