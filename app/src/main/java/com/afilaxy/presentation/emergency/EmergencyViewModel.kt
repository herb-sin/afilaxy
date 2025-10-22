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
import com.afilaxy.security.SecurityValidator
import com.afilaxy.domain.repository.EmergencyRepository
import com.afilaxy.performance.PerformanceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

class EmergencyViewModel : ViewModel() {
    private val repository = com.afilaxy.domain.repository.EmergencyRepositoryImpl()
    private val _uiState = MutableStateFlow(EmergencyUiState())
    val uiState: StateFlow<EmergencyUiState> = _uiState.asStateFlow()
    private var listenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null
    
    private val firebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    
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
        location?.let {
            _uiState.value = _uiState.value.copy(userLocation = it, isLoadingLocation = false)
            
            searchNearbyHelpers(it)
        } ?: run {
            _uiState.value = _uiState.value.copy(
                locationError = "Não foi possível obter sua localização. Tente novamente.",
                isLoadingLocation = false
            )
        }
    }

    fun setLocationError(error: String) {
        _uiState.value = _uiState.value.copy(locationError = error, isLoadingLocation = false)
    }

    private fun searchNearbyHelpers(location: Location) {
        viewModelScope.launch {
            // Require authenticated user for emergency operations
            val currentUser = try {
                com.afilaxy.security.AuthGuard.requireAuthentication()
            } catch (e: SecurityException) {
                _uiState.value = _uiState.value.copy(
                    locationError = "Autenticação necessária para emergências",
                    isLoadingLocation = false
                )
                return@launch
            }
            
            try {
                val emergency = repository.createEmergency(location)
                processEmergencyCreated(emergency, location)
            } catch (e: Exception) {
                android.util.Log.e("EmergencyViewModel", "Error creating emergency", e)
                _uiState.value = _uiState.value.copy(
                    locationError = "Erro ao criar emergência",
                    isLoadingLocation = false
                )
            }
        }
    }
    
    private suspend fun processEmergencyCreated(emergency: Emergency, location: Location) {
        try {
            val helpers = repository.findNearbyHelpers(location)
            
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
                
                repository.notifyHelpers(helpers, emergency)
                startListeningForHelperResponse(emergency.id)
            }
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                locationError = "Erro ao processar emergência",
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
        // Validate emergency ID format (prevent NoSQL injection)
        val sanitizedEmergencyId = com.afilaxy.security.InputSanitizer.sanitizeText(emergencyId)
        if (sanitizedEmergencyId.isBlank() || !sanitizedEmergencyId.matches(Regex("^[a-zA-Z0-9_-]{1,50}$"))) {
            android.util.Log.w("EmergencyViewModel", "Invalid emergency ID format")
            return
        }
        
        val currentUser = try {
            com.afilaxy.security.AuthGuard.requireAuthentication()
        } catch (e: SecurityException) {
            android.util.Log.w("EmergencyViewModel", "Authentication required for emergency listener")
            return
        }
        
        viewModelScope.launch {
            try {
                // Listener para notificações de confirmação e finalização
                listenerRegistration = firebaseFirestore.collection("users")
                    .document(currentUser.uid)
                    .collection("notifications")
                    .whereEqualTo("emergencyId", sanitizedEmergencyId)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            android.util.Log.e("EmergencyViewModel", "Erro no listener: ${error.message}")
                            return@addSnapshotListener
                        }
                        
                        snapshot?.documentChanges?.forEach { change ->
                            if (change.type == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                                val doc = change.document
                                val notificationType = doc.getString("type")
                                val rawHelperName = doc.getString("helperName")
                                val helperName = com.afilaxy.security.InputSanitizer.sanitizeName(rawHelperName)
                                
                                // Use safe display name
                                val displayName = if (helperName.isBlank()) {
                                    "Ajudante"
                                } else {
                                    helperName.take(30) // Limit length for security
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
        listenerRegistration?.remove()
        _uiState.value = EmergencyUiState(hasLocationPermission = _uiState.value.hasLocationPermission)
    }
}
