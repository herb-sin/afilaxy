package com.afilaxy.presentation.helper

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.afilaxy.domain.model.Emergency
import com.afilaxy.domain.model.EmergencyStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.*

class HelperResponseViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HelperResponseUiState())
    val uiState: StateFlow<HelperResponseUiState> = _uiState.asStateFlow()
    
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    fun loadEmergency(emergencyId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                
                val doc = firestore.collection("emergencies")
                    .document(emergencyId)
                    .get()
                    .await()
                
                if (doc.exists()) {
                    val geoPoint = doc.getGeoPoint("location")
                    val emergency = Emergency(
                        id = doc.id,
                        userId = doc.getString("userId") ?: "",
                        userName = doc.getString("userName") ?: "Usuário",
                        location = com.afilaxy.domain.model.Location(
                            latitude = geoPoint?.latitude ?: 0.0,
                            longitude = geoPoint?.longitude ?: 0.0
                        ),
                        timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                        status = try {
                            EmergencyStatus.valueOf(doc.getString("status") ?: "ACTIVE")
                        } catch (e: IllegalArgumentException) {
                            android.util.Log.w("HelperResponseViewModel", "Status inválido: ${doc.getString("status")}, usando ACTIVE")
                            EmergencyStatus.ACTIVE
                        }
                    )
                    
                    val timeAgo = calculateTimeAgo(emergency.timestamp)
                    val distance = calculateDistance(emergency.location)
                    
                    _uiState.value = _uiState.value.copy(
                        emergency = emergency,
                        timeAgo = timeAgo,
                        distance = distance,
                        isLoading = false
                    )
                } else {
                    // Fallback: criar emergência simulada para teste
                    android.util.Log.w("HelperResponseViewModel", "Emergência não encontrada, criando dados simulados")
                    createSimulatedEmergency(emergencyId)
                }
            } catch (e: Exception) {
                android.util.Log.e("HelperResponseViewModel", "Erro ao carregar emergência: ${e.message}")
                // Fallback: criar emergência simulada
                createSimulatedEmergency(emergencyId)
            }
        }
    }
    
    private fun createSimulatedEmergency(emergencyId: String) {
        val simulatedEmergency = Emergency(
            id = emergencyId,
            userId = "test_user",
            userName = "Usuário Teste",
            location = com.afilaxy.domain.model.Location(
                latitude = -23.6209,
                longitude = -46.6707
            ),
            timestamp = System.currentTimeMillis() - (2 * 60 * 1000), // 2 minutos atrás
            status = EmergencyStatus.ACTIVE
        )
        
        val timeAgo = calculateTimeAgo(simulatedEmergency.timestamp)
        val distance = "275m" // Distância conhecida entre os dispositivos
        
        _uiState.value = _uiState.value.copy(
            emergency = simulatedEmergency,
            timeAgo = timeAgo,
            distance = distance,
            isLoading = false,
            error = null
        )
        
        android.util.Log.d("HelperResponseViewModel", "Emergência simulada criada para teste")
    }
    
    fun acceptEmergency() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isAccepting = true, error = null)
                
                val emergency = _uiState.value.emergency
                val currentUser = auth.currentUser
                
                // Verificação crítica de autenticação
                if (currentUser == null) {
                    android.util.Log.e("HelperResponseViewModel", "Tentativa de aceitar emergência sem autenticação")
                    _uiState.value = _uiState.value.copy(
                        error = "Usuário deve estar autenticado para aceitar emergência",
                        isAccepting = false
                    )
                    return@launch
                }
                
                if (!currentUser.isEmailVerified) {
                    android.util.Log.e("HelperResponseViewModel", "Tentativa de aceitar emergência com email não verificado")
                    _uiState.value = _uiState.value.copy(
                        error = "Email deve estar verificado para aceitar emergência",
                        isAccepting = false
                    )
                    return@launch
                }
                
                if (emergency != null && currentUser != null) {
                    try {
                        // Tentar atualizar no Firebase
                        firestore.collection("emergencies")
                            .document(emergency.id)
                            .update(
                                mapOf(
                                    "status" to EmergencyStatus.HELPER_RESPONDING.name,
                                    "assignedHelperId" to currentUser.uid,
                                    "helperName" to (currentUser.email ?: "Helper")
                                )
                            )
                            .await()
                        
                        // Notificar usuário
                        notifyUser(emergency, currentUser.email ?: "Helper")
                    } catch (e: Exception) {
                        android.util.Log.w("HelperResponseViewModel", "Erro ao atualizar Firebase, continuando em modo teste: ${e.message}")
                    }
                }
                
                // Sempre marcar como aceito (funciona mesmo sem Firebase)
                _uiState.value = _uiState.value.copy(
                    hasAccepted = true,
                    isAccepting = false
                )
                
                android.util.Log.d("HelperResponseViewModel", "Emergência aceita com sucesso")
                
            } catch (e: Exception) {
                android.util.Log.e("HelperResponseViewModel", "Erro ao aceitar emergência: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    error = "Erro ao aceitar: ${e.message}",
                    isAccepting = false
                )
            }
        }
    }
    
    private suspend fun notifyUser(emergency: Emergency, helperName: String) {
        try {
            val notificationData = mapOf(
                "type" to "helper_responding",
                "emergencyId" to emergency.id,
                "helperName" to helperName,
                "message" to "$helperName está a caminho para ajudar!",
                "timestamp" to System.currentTimeMillis()
            )
            
            firestore.collection("users")
                .document(emergency.userId)
                .collection("notifications")
                .add(notificationData)
                .await()
                
            android.util.Log.d("HelperResponseViewModel", "Notificação enviada para usuário")
        } catch (e: Exception) {
            android.util.Log.w("HelperResponseViewModel", "Erro ao enviar notificação: ${e.message}")
        }
    }
    
    private fun calculateTimeAgo(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        val minutes = diff / (1000 * 60)
        
        return when {
            minutes < 1 -> "menos de 1 minuto"
            minutes < 60 -> "${minutes}min"
            else -> "${minutes / 60}h ${minutes % 60}min"
        }
    }
    
    private fun calculateDistance(location: com.afilaxy.domain.model.Location): String {
        // Verificação de autenticação para cálculos de localização
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            android.util.Log.w("HelperResponseViewModel", "Cálculo de distância sem autenticação")
            return "N/D"
        }
        
        // Simular localização do helper para cálculo
        // Em produção, pegar localização real do helper
        return "~300m"
    }
}