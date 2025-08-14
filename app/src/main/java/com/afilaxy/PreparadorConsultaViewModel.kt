package com.afilaxy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PreparadorConsultaViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<UiState>(UiState.Initial)
    val uiState: StateFlow<UiState> = _uiState

    fun prepararResumoConsulta(input: String) {
        _uiState.value = UiState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Aqui você integraria com a API de IA generativa
                val prompt = "Organize o seguinte relato para uma consulta médica, sem dar conselhos: $input"
                val response = chamarIA(prompt) // Função fictícia para chamada à IA
                _uiState.value = UiState.Success(response)
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Erro ao gerar resumo: ${e.message}")
            }
        }
    }

    // Função fictícia para simular chamada à IA
    private fun chamarIA(prompt: String): String {
        // Substitua por integração real com Gemini ou outra API
        return "Resumo gerado para consulta: [Exemplo de resposta da IA]"
    }
}