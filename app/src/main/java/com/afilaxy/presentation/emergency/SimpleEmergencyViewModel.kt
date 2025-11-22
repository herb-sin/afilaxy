package com.afilaxy.presentation.emergency

import android.app.Application
import android.location.Geocoder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.afilaxy.data.ChatManager
import com.afilaxy.data.LocationManager
import com.afilaxy.domain.repository.EmergencyRepository
import com.afilaxy.performance.LogOptimizer
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Locale
import javax.inject.Inject

/**
 * ViewModel simplificado que conecta diretamente com os Managers
 */
@HiltViewModel
class SimpleEmergencyViewModel @Inject constructor(
    private val application: Application,
    private val emergencyRepository: EmergencyRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(SimpleEmergencyUiState())
    val uiState: StateFlow<SimpleEmergencyUiState> = _uiState.asStateFlow()

    private var currentEmergencyId: String? = null

    fun requestEmergency() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(state = EmergencyState.WAITING)
            
            LogOptimizer.d("SimpleEmergencyViewModel", "=== INICIANDO PEDIDO DE EMERGÊNCIA ===")
            
            // Desativar helper automaticamente (quem pede ajuda não tem bombinha)
            LogOptimizer.d("SimpleEmergencyViewModel", "Desativando helper - usuário precisa de ajuda")
            emergencyRepository.deactivateHelper()
            
            val location = LocationManager.getCurrentLocation(application)
            LogOptimizer.d("SimpleEmergencyViewModel", "Localização obtida: $location")
            
            val emergencyId = if (location != null) {
                LogOptimizer.d("SimpleEmergencyViewModel", "Criando emergência em: ${location.first}, ${location.second}")
                val result = emergencyRepository.createEmergency(
                    latitude = location.first,
                    longitude = location.second
                )
                result.getOrNull()
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
            currentEmergencyId?.let { emergencyRepository.cancelEmergency(it) }
            _uiState.value = _uiState.value.copy(state = EmergencyState.IDLE)
        }
    }

    fun loadEmergency(emergencyId: String) {
        currentEmergencyId = emergencyId
        viewModelScope.launch {
            try {
                val firestore = FirebaseFirestore.getInstance()
                val emergencyDoc = firestore.collection("emergency_requests").document(emergencyId).get().await()
                
                val requesterId = emergencyDoc.getString("requesterId")
                val requesterName = emergencyDoc.getString("requesterName") ?: "Pessoa em emergência"
                val helperId = emergencyDoc.getString("helperId")
                val helperName = emergencyDoc.getString("helperName") ?: "Helper"
                val status = emergencyDoc.getString("status")
                
                val currentUserId = com.afilaxy.security.AuthGuard.getCurrentUserId()
                
                if (currentUserId == requesterId) {
                    // Sou o REQUISITANTE
                    val newState = if (status == "matched") EmergencyState.MATCHED else EmergencyState.WAITING
                    
                    _uiState.value = _uiState.value.copy(
                        state = newState,
                        requesterName = helperName, // Na visão do requester, mostramos o nome do helper
                        requesterId = requesterId,
                        helperId = helperId
                    )
                    LogOptimizer.d("SimpleEmergencyViewModel", "Emergency carregada (Requester): id=$emergencyId, state=$newState, helperId=$helperId")
                    
                    if (newState == EmergencyState.MATCHED) {
                        startListeningToChat(emergencyId)
                    }
                } else {
                    // Sou o HELPER
                    _uiState.value = _uiState.value.copy(
                        state = EmergencyState.HELPING,
                        requesterName = requesterName,
                        requesterId = requesterId,
                        helperId = helperId // Carregar helperId também para garantir consistência
                    )
                    LogOptimizer.d("SimpleEmergencyViewModel", "Emergency carregada (Helper): requesterId=$requesterId, requesterName=$requesterName")
                    
                    startListeningToChat(emergencyId)
                }
            } catch (e: Exception) {
                LogOptimizer.e("SimpleEmergencyViewModel", "Erro ao carregar emergência", e)
                _uiState.value = _uiState.value.copy(
                    state = EmergencyState.HELPING,
                    requesterName = "Erro ao carregar"
                )
            }
        }
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

    fun finishEmergency() {
        viewModelScope.launch {
            currentEmergencyId?.let { emergencyId ->
                LogOptimizer.d("SimpleEmergencyViewModel", "Finalizando emergência: $emergencyId")
                
                val result = emergencyRepository.finishEmergency(emergencyId)
                if (result.isSuccess) {
                    // Atualizar estado local
                    _uiState.value = SimpleEmergencyUiState(state = EmergencyState.IDLE)
                    currentEmergencyId = null
                    LogOptimizer.d("SimpleEmergencyViewModel", "Emergência finalizada com sucesso")
                } else {
                    LogOptimizer.e("SimpleEmergencyViewModel", "Erro ao finalizar emergência", result.exceptionOrNull())
                }
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
                        val helperId = snapshot?.getString("helperId")
                        val requesterId = snapshot?.getString("requesterId")
                        
                        LogOptimizer.d("SimpleEmergencyViewModel", "Status atualizado: $status, Helper: $helperName")
                        LogOptimizer.d("SimpleEmergencyViewModel", "Documento completo: ${snapshot?.data}")
                        
                        when (status) {
                            "matched" -> {
                                LogOptimizer.d("SimpleEmergencyViewModel", "EMERGÊNCIA ACEITA! Iniciando chat...")
                                trySend(Triple(EmergencyState.MATCHED, helperName ?: "Helper", Pair(helperId, requesterId)))
                            }
                            "cancelled" -> {
                                LogOptimizer.d("SimpleEmergencyViewModel", "Emergência cancelada")
                                trySend(Triple(EmergencyState.IDLE, "", Pair(null, null)))
                            }
                            else -> {
                                LogOptimizer.d("SimpleEmergencyViewModel", "Status não reconhecido ou ainda waiting: $status")
                            }
                        }
                    }
                
                awaitClose { listener.remove() }
            }.collect { (newState, helperName, ids) ->
                _uiState.value = _uiState.value.copy(
                    state = newState,
                    requesterName = helperName,
                    helperId = ids.first,
                    requesterId = ids.second
                )
                
                if (newState == EmergencyState.MATCHED) {
                    startListeningToChat(emergencyId)
                }
            }
        }
    }
    
    suspend fun getAddressFromLocation(latitude: Double, longitude: Double): String? {
        return try {
            val geocoder = Geocoder(application, Locale.getDefault())
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            addresses?.firstOrNull()?.getAddressLine(0)
        } catch (e: Exception) {
            LogOptimizer.e("SimpleEmergencyViewModel", "Erro ao buscar endereço", e)
            null
        }
    }
    
    suspend fun getOtherUserLocation(userId: String): Pair<Double, Double>? {
        return try {
            LogOptimizer.d("SimpleEmergencyViewModel", "getOtherUserLocation chamado para userId: $userId, emergencyId: $currentEmergencyId")
            
            val firestore = FirebaseFirestore.getInstance()
            
            // Se for requester, buscar localização da emergência
            if (currentEmergencyId != null) {
                val emergencyDoc = firestore.collection("emergency_requests").document(currentEmergencyId!!).get().await()
                val requesterId = emergencyDoc.getString("requesterId")
                LogOptimizer.d("SimpleEmergencyViewModel", "RequesterId da emergência: $requesterId, buscando userId: $userId")
                
                if (userId == requesterId) {
                    // É o requester, buscar localização da emergência
                    val location = emergencyDoc.getGeoPoint("location")
                    LogOptimizer.d("SimpleEmergencyViewModel", "Localização do requester: $location")
                    return location?.let { Pair(it.latitude, it.longitude) }
                }
            }
            
            // Se for helper, buscar na coleção helpers
            LogOptimizer.d("SimpleEmergencyViewModel", "Buscando na coleção helpers para userId: $userId")
            val helperDoc = firestore.collection("helpers").document(userId).get().await()
            
            if (helperDoc.exists()) {
                val location = helperDoc.getGeoPoint("location")
                LogOptimizer.d("SimpleEmergencyViewModel", "Helper encontrado com localização: $location")
                location?.let { Pair(it.latitude, it.longitude) }
            } else {
                // Se não está em helpers, buscar última localização conhecida
                LogOptimizer.d("SimpleEmergencyViewModel", "Helper não encontrado, buscando lastLocation em users")
                val userDoc = firestore.collection("users").document(userId).get().await()
                val location = userDoc.getGeoPoint("lastLocation")
                LogOptimizer.d("SimpleEmergencyViewModel", "LastLocation do usuário: $location")
                location?.let { Pair(it.latitude, it.longitude) }
            }
        } catch (e: Exception) {
            LogOptimizer.e("SimpleEmergencyViewModel", "Erro ao buscar localização do usuário $userId", e)
            null
        }
    }
    
    /**
     * Verifica e recarrega o estado da emergência ativa.
     * Usado quando o usuário retorna à tela de emergência.
     */
    fun checkAndReloadEmergency() {
        viewModelScope.launch {
            try {
                val firestore = FirebaseFirestore.getInstance()
                val userId = com.afilaxy.security.AuthGuard.getCurrentUserId() ?: return@launch
                
                // Buscar emergência ativa do usuário
            val emergencyQuery = firestore.collection("emergency_requests")
                .whereEqualTo("requesterId", userId)
                .whereEqualTo("status", "matched")
                .whereEqualTo("active", true)
                .get()
                .await()
            
            if (!emergencyQuery.isEmpty) {
                LogOptimizer.d("SimpleEmergencyViewModel", "Encontradas ${emergencyQuery.size()} emergências ativas")
                val emergencyDoc = emergencyQuery.documents.first()
                    val emergencyId = emergencyDoc.id
                    val helperId = emergencyDoc.getString("helperId")
                    val helperName = emergencyDoc.getString("helperName") ?: "Helper"
                    val requesterId = emergencyDoc.getString("requesterId")
                    
                    currentEmergencyId = emergencyId
                    _uiState.value = _uiState.value.copy(
                        state = EmergencyState.MATCHED,
                        requesterName = helperName,
                        helperId = helperId,
                        requesterId = requesterId
                    )
                    
                    LogOptimizer.d("SimpleEmergencyViewModel", "Emergência ativa recarregada: id=$emergencyId, helperId=$helperId")
                    
                    // Reiniciar listener do chat se necessário
                    if (_uiState.value.chatMessages.isEmpty()) {
                        startListeningToChat(emergencyId)
                    }
                }
            } catch (e: Exception) {
                LogOptimizer.e("SimpleEmergencyViewModel", "Erro ao recarregar emergência ativa", e)
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
    val requesterName: String = "",
    val helperId: String? = null,
    val requesterId: String? = null
)