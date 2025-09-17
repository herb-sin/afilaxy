package com.afilaxy.presentation.helper

import com.afilaxy.domain.model.Emergency

data class HelperResponseUiState(
    val isLoading: Boolean = false,
    val emergency: Emergency? = null,
    val distance: String? = null,
    val timeAgo: String = "",
    val isAccepting: Boolean = false,
    val hasAccepted: Boolean = false,
    val error: String? = null
)