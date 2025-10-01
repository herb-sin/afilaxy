package com.afilaxy.presentation.comunidade

import com.afilaxy.domain.model.Produto
import com.afilaxy.domain.model.Evento
import com.afilaxy.domain.model.ProjetoInfo

data class ComunidadeUiState(
    val produtos: List<Produto> = emptyList(),
    val eventos: List<Evento> = emptyList(),
    val projetos: List<ProjetoInfo> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)