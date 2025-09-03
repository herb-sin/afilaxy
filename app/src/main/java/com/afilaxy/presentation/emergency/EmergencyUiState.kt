package com.afilaxy.presentation.emergency

import com.afilaxy.domain.model.Helper
import com.afilaxy.domain.model.Location

data class EmergencyUiState(
    val hasLocationPermission: Boolean = false,
    val userLocation: Location? = null,
    val isLoadingLocation: Boolean = false,
    val locationError: String? = null,
    val nearbyHelpers: List<Helper> = emptyList(),
    val noHelpersFound: Boolean = false,
    val helperResponding: Helper? = null,
    val isAwaitingHelperResponse: Boolean = false,
    val showEmergencyInstructions: Boolean = false,
    val emergencyActive: Boolean = false
)