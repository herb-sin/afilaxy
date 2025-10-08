package com.afilaxy.presentation.autocuidado

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.afilaxy.PreparadorConsultaViewModel
import com.afilaxy.UiState
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AutocuidadoViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AutocuidadoUiState())
    val uiState: StateFlow<AutocuidadoUiState> = _uiState.asStateFlow()
    
    private val preparadorViewModel by lazy { PreparadorConsultaViewModel() }
    private val firebaseAuth by lazy { FirebaseAuth.getInstance() }
    
    fun updatePergunta(pergunta: String) {
        _uiState.value = _uiState.value.copy(pergunta = pergunta)
    }
    
    fun perguntarIA() {
        val question = _uiState.value.pergunta.take(500)
        if (question.isBlank()) return
        
        if (!com.afilaxy.security.FinalSecurityLayer.isSecureContext()) {
            com.afilaxy.security.SecurityUtils.safeLog("AutocuidadoViewModel", "AI query denied - insecure context", com.afilaxy.security.SecurityUtils.LogLevel.WARN)
            _uiState.value = _uiState.value.copy(
                resposta = UiState.Error("Acesso negado - contexto inseguro")
            )
            return
        }
        
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    resposta = UiState.Loading,
                    isLoading = true
                )
                
                preparadorViewModel.prepararResumoConsulta(question)
                
                val response = preparadorViewModel.uiState.first { it !is UiState.Loading }
                _uiState.value = _uiState.value.copy(
                    resposta = response,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    resposta = UiState.Error("Erro ao processar pergunta"),
                    isLoading = false
                )
            }
        }
    }
}