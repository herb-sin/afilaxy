package com.afilaxy.presentation.home

data class HomeUiState(
    val isLoading: Boolean = false,
    val userName: String? = null,
    val errorMessage: String? = null
)