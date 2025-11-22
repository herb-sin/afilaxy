package com.afilaxy.presentation.emergency

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.afilaxy.domain.model.ChatMessage
import com.afilaxy.domain.repository.IChatRepository
import com.afilaxy.domain.usecase.SendChatMessageUseCase
import com.afilaxy.security.AuthGuard
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EmergencyResponseViewModel @Inject constructor(
    private val chatRepository: IChatRepository,
    private val sendChatMessageUseCase: SendChatMessageUseCase
) : ViewModel() {
    
    private var emergencyId: String = ""
    private var application: android.app.Application? = null
    
    fun initialize(emergencyId: String, application: android.app.Application?) {
        this.emergencyId = emergencyId
        this.application = application
        observeChatMessages()
        loadEmergencyData()
    }
    
    var chatMessages by mutableStateOf<List<ChatMessage>>(emptyList())
        private set
        
    var requesterLocation by mutableStateOf<LatLng?>(null)
        private set
        
    var helperLocation by mutableStateOf<LatLng?>(null)
        private set
        
    var requesterName by mutableStateOf("Pessoa em emergência")
        private set
        
    var isLoading by mutableStateOf(true)
        private set
        
    var showResolveDialog by mutableStateOf(false)
        private set
        
    var canMarkAsResolved by mutableStateOf(false)
        private set
    
    fun loadEmergencyData() {
        viewModelScope.launch {
            try {
                // Obter localização real do GPS se Application estiver disponível
                val app = application
                helperLocation = if (app != null) {
                    val locationRepository = com.afilaxy.data.repository.LocationRepository(app)
                    val currentLocation = locationRepository.getCurrentLocation()
                    currentLocation?.let { 
                        LatLng(it.latitude, it.longitude) 
                    } ?: LatLng(-23.5505, -46.6333)
                } else {
                    LatLng(-23.5505, -46.6333) // Fallback para São Paulo
                }
                
                // TODO: Buscar dados reais da emergência do Firestore
                requesterLocation = LatLng(-23.5515, -46.6343) // Será substituído por dados reais
                requesterName = "Pessoa em emergência"
                canMarkAsResolved = true
                isLoading = false
            } catch (e: Exception) {
                android.util.Log.e("EmergencyResponseVM", "Error loading emergency data: ${e.javaClass.simpleName}")
                // Fallback para localização padrão
                helperLocation = LatLng(-23.5505, -46.6333)
                isLoading = false
            }
        }
    }
    
    private fun observeChatMessages() {
        viewModelScope.launch {
            chatRepository.getMessages(emergencyId).collect { messages ->
                chatMessages = messages
            }
        }
    }
    
    fun sendMessage(message: String) {
        viewModelScope.launch {
            val result = sendChatMessageUseCase.execute(
                emergencyId = emergencyId,
                message = message,
                isFromHelper = true
            )
            
            when (result) {
                is SendChatMessageUseCase.Result.Success -> {
                    // Mensagem enviada com sucesso
                }
                is SendChatMessageUseCase.Result.AuthenticationRequired -> {
                    android.util.Log.w("EmergencyResponseVM", "Authentication required for sending message")
                }
                is SendChatMessageUseCase.Result.MessageEmpty -> {
                    android.util.Log.w("EmergencyResponseVM", "Empty message not sent")
                }
                is SendChatMessageUseCase.Result.Error -> {
                    android.util.Log.e("EmergencyResponseVM", "Error sending message: ${result.message}")
                }
            }
        }
    }
    
    fun markEmergencyAsResolved() {
        showResolveDialog = true
    }
    
    fun confirmResolveEmergency() {
        viewModelScope.launch {
            try {
                // TODO: Marcar emergência como resolvida no Firestore
                // Limpar chat
                chatRepository.clearChat(emergencyId)
                showResolveDialog = false
            } catch (e: Exception) {
                android.util.Log.e("EmergencyResponseVM", "Error resolving emergency: ${e.javaClass.simpleName}")
            }
        }
    }
    
    fun dismissResolveDialog() {
        showResolveDialog = false
    }
}