package com.afilaxy.presentation.emergency

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.afilaxy.data.ChatManager
import com.afilaxy.data.EmergencyManager
import com.afilaxy.data.LocationManager
import com.afilaxy.data.SimpleMessage
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

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
            
            android.util.Log.d("SimpleEmergencyViewModel", "=== INICIANDO PEDIDO DE EMERGÊNCIA ===")
            val location = LocationManager.getCurrentLocation(application)
            android.util.Log.d("SimpleEmergencyViewModel", "Localização obtida: $location")
            
            val emergencyId = if (location != null) {
                android.util.Log.d("SimpleEmergencyViewModel", "Criando emergência em: ${location.first}, ${location.second}")
                EmergencyManager.createEmergency(
                    latitude = location.first,
                    longitude = location.second
                )
            } else {
                android.util.Log.e("SimpleEmergencyViewModel", "ERRO: Localização é null")
                null
            }
            
            if (emergencyId != null) {
                currentEmergencyId = emergencyId
                android.util.Log.d("SimpleEmergencyViewModel", "Emergência criada com ID: $emergencyId")
                android.util.Log.d("SimpleEmergencyViewModel", "Aguardando resposta de helpers...")
                
                // Aguardar resposta real por 30 segundos
                kotlinx.coroutines.delay(30000)
                
                // Se ainda está esperando, simular match
                if (_uiState.value.state == EmergencyState.WAITING) {
                    android.util.Log.d("SimpleEmergencyViewModel", "Timeout - simulando match")
                    _uiState.value = _uiState.value.copy(state = EmergencyState.MATCHED)
                    startListeningToChat(emergencyId)
                }
            } else {
                android.util.Log.e("SimpleEmergencyViewModel", "ERRO: Falha ao criar emergência")
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

    fun sendMessage(message: String) {
        viewModelScope.launch {
            currentEmergencyId?.let { emergencyId ->
                ChatManager.sendMessage(emergencyId, message)
            }
        }
    }

    private fun startListeningToChat(emergencyId: String) {
        viewModelScope.launch {
            ChatManager.listenToMessages(emergencyId).collect { messages ->
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