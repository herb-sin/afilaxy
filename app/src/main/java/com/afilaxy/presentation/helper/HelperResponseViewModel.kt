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
                        status = EmergencyStatus.valueOf(doc.getString("status") ?: "ACTIVE")
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
                    _uiState.value = _uiState.value.copy(
                        error = "Emergência não encontrada",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Erro ao carregar: ${e.message}",
                    isLoading = false
                )
            }
        }
    }
    
    fun acceptEmergency() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isAccepting = true, error = null)
                
                val emergency = _uiState.value.emergency
                val currentUser = auth.currentUser
                
                // Modo de teste: criar emergência fictícia se não existir
                if (emergency == null) {
                    // Simular aceitação para teste
                    kotlinx.coroutines.delay(1500) // Simular delay de rede
                    _uiState.value = _uiState.value.copy(
                        hasAccepted = true,
                        isAccepting = false
                    )
                    return@launch
                }
                
                if (currentUser == null) {
                    _uiState.value = _uiState.value.copy(
                        error = "Usuário não autenticado",
                        isAccepting = false
                    )
                    return@launch
                }
                
                // Atualizar status da emergência
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
                
                // Notificar usuário que helper está a caminho
                notifyUser(emergency, currentUser.email ?: "Helper")
                
                _uiState.value = _uiState.value.copy(
                    hasAccepted = true,
                    isAccepting = false
                )
                
            } catch (e: Exception) {
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
        } catch (e: Exception) {
            // Ignorar erro de notificação
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
    
    private suspend fun calculateDistance(location: com.afilaxy.domain.model.Location): String {
        // Simular localização do helper para cálculo
        // Em produção, pegar localização real do helper
        return "~300m"
    }
}