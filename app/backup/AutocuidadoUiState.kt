package com.afilaxy.presentation.autocuidado

import com.afilaxy.UiState

data class AutocuidadoUiState(
    val pergunta: String = "",
    val resposta: UiState = UiState.Idle,
    val isLoading: Boolean = false
)