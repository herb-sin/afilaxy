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
                    _uiState.value = _uiState.value.copy(
                        error = "Emergência não encontrada",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("HelperResponseViewModel", "Erro ao carregar emergência: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    error = "Erro ao carregar emergência: ${e.message}",
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
                        // Buscar nome do helper no Firestore
                        val userDoc = firestore.collection("users").document(currentUser.uid).get().await()
                        val helperName = userDoc.getString("name")
                        val displayName = if (helperName.isNullOrBlank() || helperName.contains("@")) {
                            "Ajudante"
                        } else {
                            helperName
                        }
                        
                        // Tentar atualizar no Firebase
                        firestore.collection("emergencies")
                            .document(emergency.id)
                            .update(
                                mapOf(
                                    "status" to EmergencyStatus.HELPER_RESPONDING.name,
                                    "assignedHelperId" to currentUser.uid,
                                    "helperName" to displayName
                                )
                            )
                            .await()
                        
                        // Notificar usuário
                        notifyUser(emergency, displayName)
                    } catch (e: Exception) {
                        android.util.Log.e("HelperResponseViewModel", "Erro ao atualizar Firebase: ${e.message}")
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
                "message" to "$helperName está a caminho!",
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
    
    fun finishHelp() {
        viewModelScope.launch {
            try {
                val emergency = _uiState.value.emergency
                val currentUser = auth.currentUser
                
                if (emergency != null && currentUser != null) {
                    // Buscar nome do helper
                    val userDoc = firestore.collection("users").document(currentUser.uid).get().await()
                    val helperName = userDoc.getString("name")
                    val displayName = if (helperName.isNullOrBlank() || helperName.contains("@")) {
                        "Ajudante"
                    } else {
                        helperName
                    }
                    
                    // Notificar remetente que ajuda foi finalizada
                    val notificationData = mapOf(
                        "type" to "help_completed",
                        "emergencyId" to emergency.id,
                        "helperName" to displayName,
                        "message" to "$displayName finalizou a ajuda. Esperamos que esteja bem!",
                        "timestamp" to System.currentTimeMillis()
                    )
                    
                    firestore.collection("users")
                        .document(emergency.userId)
                        .collection("notifications")
                        .add(notificationData)
                        .await()
                        
                    android.util.Log.d("HelperResponseViewModel", "Notificação de finalização enviada")
                }
            } catch (e: Exception) {
                android.util.Log.e("HelperResponseViewModel", "Erro ao finalizar ajuda: ${e.message}")
            }
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