package com.afilaxy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PreparadorConsultaViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<UiState>(UiState.Initial)
    val uiState: StateFlow<UiState> = _uiState

    private val generativeModel = GenerativeModel(
    modelName = "gemini-1.5-flash",
    apiKey = BuildConfig.GEMINI_API_KEY
)

    fun prepararResumoConsulta(input: String) {
        _uiState.value = UiState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val prompt = "Responda como um assistente de autocuidado para Asma/DPOC, usando apenas informações seguras e sem dar conselhos médicos. Pergunta: $input"
                val response = generativeModel.generateContent(
                    content { text(prompt) }
                )
                val resposta = response.text ?: "Não foi possível obter resposta da IA."
                _uiState.value = UiState.Success(resposta)
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Erro ao gerar resposta: ${e.message}")
            }
        }
    }
}