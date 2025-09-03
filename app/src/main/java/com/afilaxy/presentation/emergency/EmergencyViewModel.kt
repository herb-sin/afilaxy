package com.afilaxy.presentation.emergency

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.afilaxy.domain.model.Helper
import com.afilaxy.domain.model.Location

class EmergencyViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(EmergencyUiState())
    val uiState: StateFlow<EmergencyUiState> = _uiState.asStateFlow()
    
    fun updateLocationPermission(hasPermission: Boolean) {
        _uiState.value = _uiState.value.copy(hasLocationPermission = hasPermission)
    }
    
    fun startLocationSearch() {
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
        if (location != null) {
            _uiState.value = _uiState.value.copy(
                userLocation = location,
                isLoadingLocation = false
            )
            searchNearbyHelpers(location)
        } else {
            _uiState.value = _uiState.value.copy(
                locationError = "Não foi possível obter sua localização. Tente novamente.",
                isLoadingLocation = false
            )
        }
    }
    
    fun setLocationError(error: String) {
        _uiState.value = _uiState.value.copy(
            locationError = error,
            isLoadingLocation = false
        )
    }
    
    private fun searchNearbyHelpers(location: Location) {
        viewModelScope.launch {
            try {
                // Simular busca por helpers próximos
                delay(2000) // Simular delay de rede
                
                val dummyHelpers = listOf(
                    Helper(id = "user123", nome = "Ajudante Voluntário A", distanciaEstimada = "aprox. 150m"),
                    Helper(id = "user456", nome = "Ajudante Voluntário B", distanciaEstimada = "aprox. 400m"),
                    Helper(id = "user789", nome = "Ajudante Voluntário C", distanciaEstimada = "aprox. 750m")
                )
                
                // Simular diferentes cenários
                if (System.currentTimeMillis() % 4 == 0L) { 
                    // Sem helpers encontrados
                    _uiState.value = _uiState.value.copy(
                        nearbyHelpers = emptyList(),
                        noHelpersFound = true,
                        isAwaitingHelperResponse = false
                    )
                } else {
                    // Helpers encontrados
                    _uiState.value = _uiState.value.copy(
                        nearbyHelpers = dummyHelpers,
                        noHelpersFound = false,
                        isAwaitingHelperResponse = true,
                        helperResponding = null
                    )
                    
                    // Simular resposta de helper após alguns segundos
                    delay(5000)
                    simulateHelperResponse()
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    locationError = "Erro ao buscar ajuda próxima: ${e.message}",
                    isLoadingLocation = false
                )
            }
        }
    }
    
    private fun simulateHelperResponse() {
        val currentHelpers = _uiState.value.nearbyHelpers
        if (currentHelpers.isNotEmpty() && System.currentTimeMillis() % 3 != 0L) {
            val respondingHelper = currentHelpers.first()
            _uiState.value = _uiState.value.copy(
                helperResponding = respondingHelper,
                isAwaitingHelperResponse = false,
                emergencyActive = true
            )
        }
    }
    
    fun showEmergencyInstructions() {
        _uiState.value = _uiState.value.copy(showEmergencyInstructions = true)
    }
    
    fun hideEmergencyInstructions() {
        _uiState.value = _uiState.value.copy(showEmergencyInstructions = false)
    }
    
    fun resetEmergencyState() {
        _uiState.value = EmergencyUiState(
            hasLocationPermission = _uiState.value.hasLocationPermission
        )
    }
}