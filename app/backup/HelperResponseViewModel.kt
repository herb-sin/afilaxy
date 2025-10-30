package com.afilaxy.presentation.helper

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class HelperResponseUiState(
    val isLoading: Boolean = false,
    val emergency: com.afilaxy.domain.model.Emergency? = null,
    val distance: String? = null,
    val timeAgo: String = "",
    val isAccepting: Boolean = false,
    val hasAccepted: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class HelperResponseViewModel @Inject constructor() : ViewModel() {
    
    private val _uiState = MutableStateFlow(HelperResponseUiState())
    val uiState: StateFlow<HelperResponseUiState> = _uiState.asStateFlow()
    
    fun loadEmergency(emergencyId: String) {
        // Load emergency logic
    }
    
    fun acceptEmergency() {
        _uiState.value = _uiState.value.copy(hasAccepted = true)
    }
    
    fun finishHelp() {
        // Finish help logic
    }
}