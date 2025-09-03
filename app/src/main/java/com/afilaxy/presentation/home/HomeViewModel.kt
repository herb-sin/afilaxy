package com.afilaxy.presentation.home

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    fun loadUserData() {
        // TODO: Implementar carregamento de dados do usuário
        _uiState.value = _uiState.value.copy(isLoading = true)
        
        // Simular carregamento por enquanto
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            userName = "Usuário Afilaxy"
        )
    }
}