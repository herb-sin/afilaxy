package com.afilaxy.presentation.autocuidado

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.afilaxy.UiState
import com.afilaxy.ai.LocalRespiratoryAI

class AutocuidadoViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AutocuidadoUiState())
    val uiState: StateFlow<AutocuidadoUiState> = _uiState.asStateFlow()
    
    private val respiratoryAI = LocalRespiratoryAI()
    
    fun updatePergunta(pergunta: String) {
        _uiState.value = _uiState.value.copy(pergunta = pergunta)
    }
    
    fun perguntarIA() {
        val question = _uiState.value.pergunta?.take(500) ?: ""
        if (question.isBlank()) return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(resposta = UiState.Loading, isLoading = true)
            
            val response = try {
                UiState.Success(respiratoryAI.getAsthmaInfo(question))
            } catch (e: Exception) {
                UiState.Error("Erro ao consultar IA: ${e.message}")
            }
            
            _uiState.value = _uiState.value.copy(resposta = response, isLoading = false)
        }
    }
}