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
        if (!AuthGuard.requireAuthentication("location_permission")) {
            return
        }
        _uiState.value = _uiState.value.copy(hasLocationPermission = hasPermission)
    }

    fun startLocationSearch() {
        if (!AuthGuard.requireAuthentication("location_search")) {
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
            SecurityInterceptor.secureAsyncOperation("emergency_create") {
                val emergency = createEmergencyUseCase.execute(location)
                processEmergencyCreated(emergency, location)
            } ?: run {
                SecurityMonitor.reportSecurityEvent("EMERGENCY_VIOLATION", "Unauthorized emergency creation")
                _uiState.value = _uiState.value.copy(
                    locationError = "Operação não autorizada",
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
        SecurityInterceptor.secureOperation("emergency_listener") {
            val validationResult = CentralizedValidator.validateInput(emergencyId, CentralizedValidator.InputType.GENERAL)
            if (!validationResult.isValid) {
                SecurityMonitor.reportSecurityEvent("INJECTION_ATTEMPT", "Invalid emergency ID: $emergencyId")
                return@secureOperation
            }
            
            SecureLogger.d("EmergencyViewModel", "Emergency listener setup")
        }
    }

    fun resetEmergencyState() {
        if (!AuthGuard.requireAuthentication("reset_emergency")) {
            return
        }
        
        listenerRegistration?.remove()
        _uiState.value = EmergencyUiState(hasLocationPermission = _uiState.value.hasLocationPermission)
        SecureLogger.d("EmergencyViewModel", "Emergency state reset")
    }
}
