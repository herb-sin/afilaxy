package com.afilaxy.presentation.emergency

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afilaxy.domain.model.Emergency
import com.afilaxy.domain.model.EmergencyStatus
import com.afilaxy.domain.model.Helper
import com.afilaxy.domain.model.Location
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlin.math.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.afilaxy.security.AuthGuard
import com.afilaxy.security.InputSanitizer
import com.afilaxy.domain.repository.EmergencyRepository
import com.afilaxy.domain.repository.EmergencyRepositoryImpl
import com.afilaxy.domain.usecase.CreateEmergencyUseCase
import com.afilaxy.domain.usecase.FindHelpersUseCase
import com.afilaxy.data.preload.HelperPreloader
import com.afilaxy.utils.ErrorHandler

class EmergencyViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(EmergencyUiState())
    val uiState: StateFlow<EmergencyUiState> = _uiState.asStateFlow()
    private var listenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null
    
    // Instâncias otimizadas
    private val firebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val repository: EmergencyRepository = EmergencyRepositoryImpl()
    private val createEmergencyUseCase = CreateEmergencyUseCase(repository)
    private val findHelpersUseCase = FindHelpersUseCase(repository)
    private val helperPreloader = HelperPreloader(repository)
    
    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }

    fun updateLocationPermission(hasPermission: Boolean) {
        _uiState.value = _uiState.value.copy(hasLocationPermission = hasPermission)
    }

    fun startLocationSearch() {
        _uiState.value =
                _uiState.value.copy(
                        isLoadingLocation = true,
                        locationError = null,
                        userLocation = null,
                        nearbyHelpers = emptyList(),
                        noHelpersFound = false,
                        isAwaitingHelperResponse = false,
                        helperResponding = null
                )
    }

    fun setLocation(location: Location?) {
        if (location != null) {
            _uiState.value = _uiState.value.copy(userLocation = location, isLoadingLocation = false)
            // Preload helpers em background
            helperPreloader.preloadNearbyHelpers(location)
            searchNearbyHelpers(location)
        } else {
            _uiState.value =
                    _uiState.value.copy(
                            locationError =
                                    "Não foi possível obter sua localização. Tente novamente.",
                            isLoadingLocation = false
                    )
        }
    }

    fun setLocationError(error: String) {
        _uiState.value = _uiState.value.copy(locationError = error, isLoadingLocation = false)
    }

    private fun searchNearbyHelpers(location: Location) {
        if (!AuthGuard.isAuthenticated()) {
            _uiState.value = _uiState.value.copy(
                locationError = "Usuário não autenticado",
                isLoadingLocation = false
            )
            return
        }
        
        viewModelScope.launch {
            // Criar emergência
            createEmergencyUseCase(location)
                .onSuccess { emergency ->
                    processEmergencyCreated(emergency, location)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        locationError = error.message ?: "Erro ao criar emergência",
                        isLoadingLocation = false
                    )
                }
        }
    }
    
    private suspend fun processEmergencyCreated(emergency: Emergency, location: Location) {
        // Buscar helpers
        findHelpersUseCase(location)
            .onSuccess { helpers ->
                if (helpers.isEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        nearbyHelpers = emptyList(),
                        noHelpersFound = true,
                        isAwaitingHelperResponse = false,
                        emergencyId = emergency.id
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        nearbyHelpers = helpers,
                        noHelpersFound = false,
                        isAwaitingHelperResponse = true,
                        helperResponding = null,
                        emergencyId = emergency.id
                    )
                    
                    try {
                        repository.notifyHelpers(helpers, emergency)
                        startListeningForHelperResponse(emergency.id)
                    } catch (e: Exception) {
                        android.util.Log.e("EmergencyViewModel", "Erro ao notificar helpers")
                    }
                }
            }
            .onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    locationError = error.message ?: "Erro ao buscar helpers",
                    isLoadingLocation = false
                )
            }
    }



    fun showEmergencyInstructions() {
        _uiState.value = _uiState.value.copy(showEmergencyInstructions = true)
    }

    fun hideEmergencyInstructions() {
        _uiState.value = _uiState.value.copy(showEmergencyInstructions = false)
    }

    fun startListeningForHelperResponse(emergencyId: String) {
        val currentUser = AuthGuard.getCurrentUser()
        if (currentUser == null) {
            android.util.Log.w("EmergencyViewModel", "Usuário não autenticado")
            return
        }
        
        viewModelScope.launch {
            try {
                // Listener para notificações de confirmação e finalização
                listenerRegistration = firebaseFirestore.collection("users")
                    .document(currentUser.uid)
                    .collection("notifications")
                    .whereEqualTo("emergencyId", emergencyId)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            android.util.Log.e("EmergencyViewModel", "Erro no listener: ${error.message}")
                            return@addSnapshotListener
                        }
                        
                        snapshot?.documentChanges?.forEach { change ->
                            if (change.type == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                                val doc = change.document
                                val notificationType = doc.getString("type")
                                val helperName = InputSanitizer.sanitizeText(doc.getString("helperName")) ?: "Helper"
                                
                                // Usar nome amigável se for email ou vazio
                                val displayName = if (helperName.isBlank() || helperName.contains("@")) {
                                    "Ajudante"
                                } else {
                                    helperName
                                }
                                
                                when (notificationType) {
                                    "helper_responding" -> {
                                        android.util.Log.d("EmergencyViewModel", "Helper respondeu positivamente")
                                        
                                        val helper = Helper(
                                            id = "responding_helper",
                                            nome = displayName,
                                            distanciaEstimada = "A caminho"
                                        )
                                        
                                        _uiState.value = _uiState.value.copy(
                                            helperResponding = helper,
                                            isAwaitingHelperResponse = false
                                        )
                                    }
                                    
                                    "help_completed" -> {
                                        android.util.Log.d("EmergencyViewModel", "Ajuda finalizada pelo helper")
                                        
                                        _uiState.value = _uiState.value.copy(
                                            helperResponding = null,
                                            isAwaitingHelperResponse = false,
                                            helpCompleted = true
                                        )
                                    }
                                }
                                
                                // Marcar como processado
                                doc.reference.update("processed", true)
                            }
                        }
                    }
            } catch (e: Exception) {
                android.util.Log.e("EmergencyViewModel", "Erro ao configurar listener: ${e.message}")
            }
        }
    }

    fun resetEmergencyState() {
        // Verificação de autenticação para operações de estado
        if (!AuthGuard.isAuthenticated()) {
            android.util.Log.w("EmergencyViewModel", "Reset de estado sem autenticação")
        }
        
        _uiState.value =
                EmergencyUiState(hasLocationPermission = _uiState.value.hasLocationPermission)
    }
}
