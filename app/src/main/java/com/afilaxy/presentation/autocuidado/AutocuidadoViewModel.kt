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
    
    private val preparadorViewModel = PreparadorConsultaViewModel()
    
    fun updatePergunta(pergunta: String) {
        _uiState.value = _uiState.value.copy(pergunta = pergunta)
    }
    
    fun perguntarIA() {
        val pergunta = _uiState.value.pergunta
        if (pergunta.isBlank()) return
        
        // Verificação crítica de autenticação
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        if (user == null) {
            android.util.Log.e("AutocuidadoViewModel", "Tentativa de usar IA sem autenticação")
            _uiState.value = _uiState.value.copy(
                resposta = UiState.Error("Usuário deve estar autenticado para usar a IA")
            )
            return
        }
        
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    resposta = UiState.Loading,
                    isLoading = true
                )
                
                // Usar o PreparadorConsultaViewModel existente
                preparadorViewModel.prepararResumoConsulta(pergunta)
                
                // Obter primeira resposta do preparador
                val resposta = preparadorViewModel.uiState.first { it !is UiState.Loading }
                _uiState.value = _uiState.value.copy(
                    resposta = resposta,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    resposta = UiState.Error("Erro ao processar pergunta: ${e.message}"),
                    isLoading = false
                )
            }
        }
    }
}