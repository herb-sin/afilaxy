package com.afilaxy

sealed class UiState {
    object Idle : UiState()
    object Initial : UiState()
    object Loading : UiState()
    data class Success(val content: String) : UiState()
    data class Error(val message: String) : UiState()
}