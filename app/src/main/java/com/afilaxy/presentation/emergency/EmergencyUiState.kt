package com.afilaxy.presentation.emergency

import com.afilaxy.domain.model.Emergency
import com.afilaxy.domain.model.Helper

data class EmergencyUiState(
    val isLoading: Boolean = false,
    val emergency: Emergency? = null,
    val nearbyHelpers: List<Helper> = emptyList(),
    val error: String? = null,
    val isLocationEnabled: Boolean = false,
    val isOffline: Boolean = false,
    val retryCount: Int = 0,
    val hasLocationPermission: Boolean = false,
    val isLoadingLocation: Boolean = false,
    val userLocation: com.afilaxy.domain.model.Location? = null,
    val locationError: String? = null,
    val noHelpersFound: Boolean = false,
    val isAwaitingHelperResponse: Boolean = false,
    val helperResponding: Helper? = null,
    val helpCompleted: Boolean = false,
    val showEmergencyInstructions: Boolean = false,
    val emergencyId: String? = null
)

sealed class EmergencyEvent {
    object CreateEmergency : EmergencyEvent()
    object RefreshHelpers : EmergencyEvent()
    object CancelEmergency : EmergencyEvent()
    data class RateHelper(val helperId: String, val rating: Int, val feedback: String) : EmergencyEvent()
    object ClearError : EmergencyEvent()
}