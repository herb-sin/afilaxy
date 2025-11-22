package com.afilaxy.presentation.emergency

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
// import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.afilaxy.performance.LogOptimizer
import com.afilaxy.performance.MapsPerformanceOptimizer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyBaseScreen(
    navController: NavController,
    emergencyId: String? = null,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val viewModel: SimpleEmergencyViewModel = androidx.hilt.navigation.compose.hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    
    // Obter localização real para o mapa
    var userLocation by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var otherUserLocation by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var userAddress by remember { mutableStateOf<String?>(null) }
    var otherUserAddress by remember { mutableStateOf<String?>(null) }
    val cameraPositionState = rememberCameraPositionState()
    
    LaunchedEffect(Unit) {
        LogOptimizer.d("EmergencyBaseScreen", "Obtendo localização para mapa...")
        val location = com.afilaxy.data.LocationManager.getCurrentLocation(context)
        LogOptimizer.d("EmergencyBaseScreen", "Localização obtida: $location")
        
        if (location != null) {
            userLocation = location
            // Centralizar mapa na localização real
            cameraPositionState.position = CameraPosition.fromLatLngZoom(
                LatLng(location.first, location.second),
                15f
            )
            LogOptimizer.d("EmergencyBaseScreen", "Mapa centralizado em: ${location.first}, ${location.second}")
            
            // Buscar endereço do usuário
            try {
                userAddress = viewModel.getAddressFromLocation(location.first, location.second)
            } catch (e: Exception) {
                LogOptimizer.e("EmergencyBaseScreen", "Erro ao buscar endereço do usuário", e)
            }
        } else {
            LogOptimizer.w("EmergencyBaseScreen", "Sem permissão de localização - mapa não será exibido")
            // Não definir userLocation - deixar null para mostrar mensagem de permissão
        }
    }
    
    // Recarregar emergência ativa quando a tela é retomada (sem emergencyId nos parâmetros)
    LaunchedEffect(Unit) {
        if (emergencyId == null) {
            viewModel.checkAndReloadEmergency()
        }
    }
    
    LaunchedEffect(emergencyId) {
        if (emergencyId != null) {
            viewModel.loadEmergency(emergencyId)
        }
    }
    
    // Buscar localização do outro usuário quando há match
    LaunchedEffect(uiState.state, uiState.helperId, uiState.requesterId) {
        if (uiState.state == EmergencyState.MATCHED || uiState.state == EmergencyState.HELPING) {
            val otherUserId = if (uiState.state == EmergencyState.MATCHED) uiState.helperId else uiState.requesterId
            otherUserId?.let { userId ->
                try {
                    val location = viewModel.getOtherUserLocation(userId)
                    otherUserLocation = location
                    LogOptimizer.d("EmergencyBaseScreen", "Localização do outro usuário: $location")
                    
                    // Buscar endereço do outro usuário
                    location?.let {
                        otherUserAddress = viewModel.getAddressFromLocation(it.first, it.second)
                    }
                } catch (e: Exception) {
                    LogOptimizer.e("EmergencyBaseScreen", "Erro ao buscar localização do outro usuário", e)
                }
            }
        } else {
            otherUserLocation = null
            otherUserAddress = null
        }
    }
    
    // Estado do teclado/foco no chat
    var isChatFocused by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    
    Column(modifier = modifier.fillMaxSize()) {
        // Mapa - se adapta ao estado do chat
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(if (isChatFocused && (uiState.state == EmergencyState.MATCHED || uiState.state == EmergencyState.HELPING)) 0.3f else 0.7f)
        ) {
            // Só mostrar mapa quando tiver localização
            if (userLocation != null) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    onMapLoaded = {
                        // Otimizar mapa após carregamento
                        // MapsPerformanceOptimizer.optimizeMap será chamado internamente
                    }
                ) {
                    // Marcador do usuário
                    userLocation?.let { location ->
                        val title = if (userAddress != null) "Você - $userAddress" else "Você"
                        Marker(
                            state = MarkerState(
                                position = LatLng(location.first, location.second)
                            ),
                            title = title
                        )
                    }
                    
                    // Marcador do outro usuário (helper ou requester)
                    otherUserLocation?.let { location ->
                        val baseTitle = if (uiState.state == EmergencyState.MATCHED) "Helper" else "Pessoa em emergência"
                        val title = if (otherUserAddress != null) "$baseTitle - $otherUserAddress" else baseTitle
                        Marker(
                            state = MarkerState(
                                position = LatLng(location.first, location.second)
                            ),
                            title = title
                        )
                    }
                }
            } else {
                // Mensagem quando não tem localização
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "📍 Localização Necessária",
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Permita o acesso à localização para usar o mapa e funcionalidades de emergência.",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        
        // Bottom Sheet Adaptativo - expande quando chat está focado
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(if (isChatFocused && (uiState.state == EmergencyState.MATCHED || uiState.state == EmergencyState.HELPING)) 0.7f else 0.3f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            when (uiState.state) {
                EmergencyState.IDLE -> IdleContent(
                    onRequestEmergency = { viewModel.requestEmergency() }
                )
                EmergencyState.WAITING -> WaitingContent(
                    onCancel = { viewModel.cancelEmergency() }
                )
                EmergencyState.MATCHED -> ChatContent(
                    messages = uiState.chatMessages,
                    onSendMessage = { message -> viewModel.sendMessage(message) },
                    onFocusChanged = { focused -> isChatFocused = focused },
                    onFinishEmergency = { viewModel.finishEmergency() }
                )
                EmergencyState.HELPING -> HelpingContent(
                    requesterName = uiState.requesterName,
                    messages = uiState.chatMessages,
                    onSendMessage = { message -> viewModel.sendMessage(message) },
                    onFocusChanged = { focused -> isChatFocused = focused }
                )
            }
        }
    }
}

@Composable
private fun IdleContent(onRequestEmergency: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = onRequestEmergency,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
        ) {
            Text(
                text = "🆘 SOLICITAR AJUDA",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun WaitingContent(onCancel: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(16.dp))
        Text("Procurando helpers próximos...")
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(onClick = onCancel) {
            Text("Cancelar")
        }
    }
}

@Composable
private fun ChatContent(
    messages: List<ChatMessage>,
    onSendMessage: (String) -> Unit,
    onFocusChanged: (Boolean) -> Unit = {},
    onFinishEmergency: () -> Unit = {}
) {
    var messageText by remember { mutableStateOf("") }
    var showFinishDialog by remember { mutableStateOf(false) }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Header com título e botão finalizar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "💬 Chat de Emergência",
                fontWeight = FontWeight.Bold
            )
            OutlinedButton(
                onClick = { showFinishDialog = true },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("✓ Finalizar")
            }
        }
        
        // Dialog de confirmação
        if (showFinishDialog) {
            AlertDialog(
                onDismissRequest = { showFinishDialog = false },
                title = { Text("Finalizar Atendimento?") },
                text = { Text("Tem certeza que deseja finalizar esta emergência? Esta ação não pode ser desfeita.") },
                confirmButton = {
                    Button(
                        onClick = {
                            showFinishDialog = false
                            onFinishEmergency()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Finalizar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showFinishDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
        
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            items(messages) { message ->
                MessageBubble(message = message)
            }
        }
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                modifier = Modifier
                    .weight(1f)
                    .onFocusChanged { focusState ->
                        onFocusChanged(focusState.isFocused)
                    },
                placeholder = { Text("Digite sua mensagem...") }
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (messageText.isNotBlank()) {
                        onSendMessage(messageText)
                        messageText = ""
                    }
                }
            ) {
                Icon(Icons.Default.Send, contentDescription = "Enviar")
            }
        }
    }
}

@Composable
private fun HelpingContent(
    requesterName: String,
    messages: List<ChatMessage>,
    onSendMessage: (String) -> Unit,
    onFocusChanged: (Boolean) -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "🆘 Ajudando: $requesterName",
            modifier = Modifier.padding(16.dp),
            fontWeight = FontWeight.Bold,
            color = Color.Red
        )
        
        ChatContent(
            messages = messages, 
            onSendMessage = onSendMessage,
            onFocusChanged = onFocusChanged
        )
    }
}

@Composable
private fun MessageBubble(message: ChatMessage) {
    val isCurrentUser = message.isFromCurrentUser
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isCurrentUser) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text = message.message,
                modifier = Modifier.padding(12.dp),
                color = if (isCurrentUser) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

data class ChatMessage(
    val id: String = "",
    val senderId: String = "",
    val message: String = "",
    val timestamp: Long = 0L,
    val isFromCurrentUser: Boolean = false
)

enum class EmergencyState {
    IDLE, WAITING, MATCHED, HELPING
}