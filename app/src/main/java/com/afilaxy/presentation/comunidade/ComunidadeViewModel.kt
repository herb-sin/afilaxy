package com.afilaxy.presentation.comunidade

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.afilaxy.domain.model.Produto
import com.afilaxy.domain.model.Evento
import com.afilaxy.domain.model.ProjetoInfo

class ComunidadeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ComunidadeUiState())
    val uiState: StateFlow<ComunidadeUiState> = _uiState.asStateFlow()
    
    init {
        loadComunidadeData()
    }
    
    private fun loadComunidadeData() {
        if (_uiState.value.isLoading) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            try {
                val data = getCommunityData()
                _uiState.update { 
                    it.copy(
                        produtos = data.produtos,
                        eventos = data.eventos,
                        projetos = data.projetos,
                        isLoading = false
                    ) 
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = "Erro ao carregar dados da comunidade"
                    ) 
                }
            }
        }
    }
    
    private fun getCommunityData(): CommunityData {
        return CommunityData(
            produtos = emptyList(),
            eventos = emptyList(),
            projetos = emptyList()
        )
    }
    
    private data class CommunityData(
        val produtos: List<Produto>,
        val eventos: List<Evento>,
        val projetos: List<ProjetoInfo>
    )
}