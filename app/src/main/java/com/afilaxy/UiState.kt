package com.afilaxy

sealed class UiState {
    object Initial : UiState()
    object Loading : UiState()
    data class Success(val resumo: String) : UiState()
    data class Error(val message: String) : UiState()
}