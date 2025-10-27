package com.afilaxy.presentation.emergency

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afilaxy.domain.model.Emergency
import com.afilaxy.domain.model.Helper
import com.afilaxy.domain.model.Location
import com.afilaxy.domain.usecase.CreateEmergencyUseCase
import com.afilaxy.domain.usecase.FindHelpersUseCase
import com.afilaxy.domain.usecase.NotifyHelpersUseCase
import com.afilaxy.security.AuthGuard
import com.afilaxy.security.SecureLogger
import com.afilaxy.security.SecurityMonitor
import com.afilaxy.security.CentralizedValidator
import com.afilaxy.security.ValidationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EmergencyViewModel @Inject constructor(
    private val createEmergencyUseCase: CreateEmergencyUseCase,
    private val findHelpersUseCase: FindHelpersUseCase,
    private val notifyHelpersUseCase: NotifyHelpersUseCase,
    private val authGuard: AuthGuard
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(EmergencyUiState())
    val uiState: StateFlow<EmergencyUiState> = _uiState.asStateFlow()
    private var listenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null
    
    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }

    fun updateLocationPermission(hasPermission: Boolean) {
        if (!AuthGuard.isUserAuthenticated()) {
            return
        }
        _uiState.value = _uiState.value.copy(hasLocationPermission = hasPermission)
    }

    fun startLocationSearch() {
        if (!AuthGuard.isUserAuthenticated()) {
            _uiState.value = _uiState.value.copy(locationError = "Authentication required")
            return
        }
        
        _uiState.value = _uiState.value.copy(
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
            try {
                // Security validation
                if (!AuthGuard.isUserAuthenticated()) {
                    SecurityMonitor.logThreat("UNAUTHORIZED_EMERGENCY", "Unauthenticated emergency creation attempt")
                    _uiState.value = _uiState.value.copy(
                        locationError = "Autenticação necessária",
                        isLoadingLocation = false
                    )
                    return@launch
                }
                
                // Coordinate validation
                val validationResult = CentralizedValidator.validateInput(
                    "${location.latitude},${location.longitude}", 
                    CentralizedValidator.InputType.COORDINATE
                )
                
                if (validationResult !is ValidationResult.Valid) {
                    SecurityMonitor.logThreat("INVALID_COORDINATES", "Invalid emergency coordinates")
                    _uiState.value = _uiState.value.copy(
                        locationError = "Coordenadas inválidas",
                        isLoadingLocation = false
                    )
                    return@launch
                }
                
                val emergency = Emergency.create(
                    id = System.currentTimeMillis().toString(),
                    userId = "current_user",
                    userName = "User",
                    location = location
                )
                
                val result = createEmergencyUseCase.execute(emergency)
                result.fold(
                    onSuccess = { emergencyId ->
                        SecureLogger.d("EmergencyViewModel", "Emergency created with ID: $emergencyId")
                        processEmergencyCreated(emergency, location)
                    },
                    onFailure = { error ->
                        SecurityMonitor.logThreat("EMERGENCY_CREATION_FAILED", error.message ?: "Unknown error")
                        _uiState.value = _uiState.value.copy(
                            locationError = "Falha ao criar emergência",
                            isLoadingLocation = false
                        )
                    }
                )
            } catch (e: Exception) {
                SecurityMonitor.logThreat("EMERGENCY_EXCEPTION", e.message ?: "Unknown exception")
                _uiState.value = _uiState.value.copy(
                    locationError = "Erro ao criar emergência",
                    isLoadingLocation = false
                )
            }
        }
    }
    
    private suspend fun processEmergencyCreated(emergency: Emergency, location: Location) {
        try {
            val helpers = findHelpersUseCase.execute(location)
            
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
                
                notifyHelpersUseCase.execute(helpers, emergency)
                startListeningForHelperResponse(emergency.id)
            }
        } catch (e: Exception) {
            SecureLogger.e("EmergencyViewModel", "Error processing emergency", e)
            _uiState.value = _uiState.value.copy(
                locationError = "Failed to process emergency",
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
        if (emergencyId.isNotBlank() && emergencyId.length < 100) {
            SecureLogger.d("EmergencyViewModel", "Emergency listener setup")
        }
    }

    fun resetEmergencyState() {
        if (!AuthGuard.isUserAuthenticated()) {
            return
        }
        
        listenerRegistration?.remove()
        _uiState.value = EmergencyUiState(hasLocationPermission = _uiState.value.hasLocationPermission)
        SecureLogger.d("EmergencyViewModel", "Emergency state reset")
    }
}
