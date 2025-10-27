package com.afilaxy.presentation.home

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class HomeUiState(
    val isHelper: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    fun loadUserData() {
        // Load user data logic
    }
    
    fun updateUserLocation(lat: Double, lon: Double) {
        // Update location logic
    }
    
    fun toggleHelperStatus() {
        _uiState.value = _uiState.value.copy(isHelper = !_uiState.value.isHelper)
    }
}