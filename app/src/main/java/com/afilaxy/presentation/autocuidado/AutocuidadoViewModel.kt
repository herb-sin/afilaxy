package com.afilaxy.presentation.autocuidado

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.afilaxy.PreparadorConsultaViewModel
import com.afilaxy.UiState
import kotlinx.coroutines.launch

class AutocuidadoViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AutocuidadoUiState())
    val uiState: StateFlow<AutocuidadoUiState> = _uiState.asStateFlow()
    
    private val preparadorViewModel = PreparadorConsultaViewModel()
    
    fun updatePergunta(pergunta: String) {
        _uiState.value = _uiState.value.copy(pergunta = pergunta)
    }
    
    fun perguntarIA() {
        val pergunta = _uiState.value.pergunta
        if (pergunta.isBlank()) return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                resposta = UiState.Loading,
                isLoading = true
            )
            
            // Usar o PreparadorConsultaViewModel existente
            preparadorViewModel.prepararResumoConsulta(pergunta)
            
            // Escutar mudanças no estado do preparador
            preparadorViewModel.uiState.collect { resposta ->
                _uiState.value = _uiState.value.copy(
                    resposta = resposta,
                    isLoading = resposta is UiState.Loading
                )
            }
        }
    }
}