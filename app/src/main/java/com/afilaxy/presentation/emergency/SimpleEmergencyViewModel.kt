package com.afilaxy.presentation.emergency

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.afilaxy.data.ChatManager
import com.afilaxy.data.EmergencyManager
import com.afilaxy.data.LocationManager
import com.afilaxy.data.SimpleMessage
import com.afilaxy.performance.LogOptimizer
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

/**
 * ViewModel simplificado que conecta diretamente com os Managers
 */
class SimpleEmergencyViewModel(private val application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(SimpleEmergencyUiState())
    val uiState: StateFlow<SimpleEmergencyUiState> = _uiState.asStateFlow()

    private var currentEmergencyId: String? = null

    fun requestEmergency() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(state = EmergencyState.WAITING)
            
            LogOptimizer.d("SimpleEmergencyViewModel", "=== INICIANDO PEDIDO DE EMERGÊNCIA ===")
            
            // Desativar helper automaticamente (quem pede ajuda não tem bombinha)
            LogOptimizer.d("SimpleEmergencyViewModel", "Desativando helper - usuário precisa de ajuda")
            EmergencyManager.deactivateHelper()
            
            val location = LocationManager.getCurrentLocation(application)
            LogOptimizer.d("SimpleEmergencyViewModel", "Localização obtida: $location")
            
            val emergencyId = if (location != null) {
                LogOptimizer.d("SimpleEmergencyViewModel", "Criando emergência em: ${location.first}, ${location.second}")
                EmergencyManager.createEmergency(
                    latitude = location.first,
                    longitude = location.second
                )
            } else {
                LogOptimizer.e("SimpleEmergencyViewModel", "ERRO: Localização é null")
                null
            }
            
            if (emergencyId != null) {
                currentEmergencyId = emergencyId
                LogOptimizer.d("SimpleEmergencyViewModel", "Emergência criada com ID: $emergencyId")
                LogOptimizer.d("SimpleEmergencyViewModel", "Aguardando resposta real de helpers...")
                
                // Escutar mudanças de status da emergência
                listenToEmergencyStatus(emergencyId)
            } else {
                LogOptimizer.e("SimpleEmergencyViewModel", "ERRO: Falha ao criar emergência")
                _uiState.value = _uiState.value.copy(state = EmergencyState.IDLE)
            }
        }
    }

    fun cancelEmergency() {
        viewModelScope.launch {
            currentEmergencyId?.let { EmergencyManager.cancelEmergency(it) }
            _uiState.value = _uiState.value.copy(state = EmergencyState.IDLE)
        }
    }

    fun loadEmergency(emergencyId: String) {
        currentEmergencyId = emergencyId
        _uiState.value = _uiState.value.copy(
            state = EmergencyState.HELPING,
            requesterName = "Pessoa em emergência"
        )
        startListeningToChat(emergencyId)
    }
    
    fun acceptEmergency() {
        // Quando helper aceita, muda para estado MATCHED
        _uiState.value = _uiState.value.copy(state = EmergencyState.MATCHED)
        currentEmergencyId?.let { startListeningToChat(it) }
    }

    fun sendMessage(message: String) {
        viewModelScope.launch {
            currentEmergencyId?.let { emergencyId ->
                ChatManager.sendMessage(emergencyId, message)
            }
        }
    }

    private fun listenToEmergencyStatus(emergencyId: String) {
        viewModelScope.launch {
            val firestore = FirebaseFirestore.getInstance()
            
            callbackFlow {
                val listener = firestore.collection("emergency_requests")
                    .document(emergencyId)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) return@addSnapshotListener
                        
                        val status = snapshot?.getString("status")
                        val helperName = snapshot?.getString("helperName")
                        
                        LogOptimizer.d("SimpleEmergencyViewModel", "Status atualizado: $status, Helper: $helperName")
                        LogOptimizer.d("SimpleEmergencyViewModel", "Documento completo: ${snapshot?.data}")
                        
                        when (status) {
                            "matched" -> {
                                LogOptimizer.d("SimpleEmergencyViewModel", "EMERGÊNCIA ACEITA! Iniciando chat...")
                                trySend(EmergencyState.MATCHED to (helperName ?: "Helper"))
                            }
                            "cancelled" -> {
                                LogOptimizer.d("SimpleEmergencyViewModel", "Emergência cancelada")
                                trySend(EmergencyState.IDLE to "")
                            }
                            else -> {
                                LogOptimizer.d("SimpleEmergencyViewModel", "Status não reconhecido ou ainda waiting: $status")
                            }
                        }
                    }
                
                awaitClose { listener.remove() }
            }.collect { (newState, helperName) ->
                _uiState.value = _uiState.value.copy(
                    state = newState,
                    requesterName = helperName
                )
                
                if (newState == EmergencyState.MATCHED) {
                    startListeningToChat(emergencyId)
                }
            }
        }
    }
    
    private fun startListeningToChat(emergencyId: String) {
        LogOptimizer.d("SimpleEmergencyViewModel", "INICIANDO ESCUTA DO CHAT para emergencyId: $emergencyId")
        viewModelScope.launch {
            ChatManager.listenToMessages(emergencyId).collect { messages ->
                LogOptimizer.d("SimpleEmergencyViewModel", "Mensagens recebidas: ${messages.size}")
                val chatMessages = messages.map { msg ->
                    ChatMessage(
                        id = msg.id,
                        senderId = msg.senderId,
                        message = msg.message,
                        timestamp = msg.timestamp,
                        isFromCurrentUser = msg.isFromCurrentUser
                    )
                }
                _uiState.value = _uiState.value.copy(chatMessages = chatMessages)
            }
        }
    }
}

data class SimpleEmergencyUiState(
    val state: EmergencyState = EmergencyState.IDLE,
    val chatMessages: List<ChatMessage> = emptyList(),
    val requesterName: String = ""
)