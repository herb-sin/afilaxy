package com.afilaxy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.afilaxy.security.SecureLogger

@HiltViewModel
class PreparadorConsultaViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState>(UiState.Initial)
    val uiState: StateFlow<UiState> = _uiState

    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = System.getenv("GEMINI_API_KEY") ?: "demo_key"
    )

    fun prepararResumoConsulta(input: String) {
        // Verificação crítica de autenticação
        val user = firebaseAuth.currentUser
        if (user == null) {
            SecureLogger.e("PreparadorConsultaViewModel", "Tentativa de usar IA sem autenticação")
            _uiState.value = UiState.Error("Usuário deve estar autenticado para usar a IA")
            return
        }
        
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