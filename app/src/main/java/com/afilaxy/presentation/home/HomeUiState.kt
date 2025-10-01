package com.afilaxy.presentation.home

data class HomeUiState(
    val isLoading: Boolean = false,
    val userName: String? = null,
    val isHelper: Boolean = true, // Por padrão é helper
    val errorMessage: String? = null
)