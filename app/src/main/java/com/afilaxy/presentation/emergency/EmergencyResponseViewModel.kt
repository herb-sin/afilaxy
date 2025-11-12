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
import kotlinx.coroutines.launch

class EmergencyResponseViewModel(
    private val emergencyId: String,
    private val chatRepository: IChatRepository,
    private val sendChatMessageUseCase: SendChatMessageUseCase
) : ViewModel() {
    
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
    
    init {
        observeChatMessages()
    }
    
    fun loadEmergencyData() {
        viewModelScope.launch {
            try {
                // TODO: Carregar dados da emergência do Firestore
                // Por enquanto, usar dados mock
                requesterLocation = LatLng(-23.5505, -46.6333) // Mock location
                helperLocation = LatLng(-23.5515, -46.6343) // Mock helper location
                requesterName = "João Silva"
                canMarkAsResolved = true
                isLoading = false
            } catch (e: Exception) {
                android.util.Log.e("EmergencyResponseVM", "Error loading emergency data: ${e.javaClass.simpleName}")
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