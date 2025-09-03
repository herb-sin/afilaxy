package com.afilaxy.presentation.autocuidado

import com.afilaxy.UiState

data class AutocuidadoUiState(
    val pergunta: String = "",
    val resposta: UiState = UiState.Initial,
    val isLoading: Boolean = false
)