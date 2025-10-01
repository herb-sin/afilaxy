package com.afilaxy

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BakingViewModel : ViewModel() {
    private val _uiState: MutableStateFlow<UiState> =
        MutableStateFlow(UiState.Initial)
    val uiState: StateFlow<UiState> =
        _uiState.asStateFlow()

    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    fun sendPrompt(
        bitmap: Bitmap,
        prompt: String
    ) {
        // Verificação crítica de autenticação
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        if (user == null) {
            android.util.Log.e("BakingViewModel", "Tentativa de usar IA sem autenticação")
            _uiState.value = UiState.Error("Usuário deve estar autenticado para usar a IA")
            return
        }
        
        _uiState.value = UiState.Loading

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = generativeModel.generateContent(
                    content {
                        image(bitmap)
                        text(prompt)
                    }
                )
                response.text?.let { outputContent ->
                    _uiState.value = UiState.Success(outputContent)
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.localizedMessage ?: "")
            }
        }
    }
    
    fun sendPromptWithResource(
        resources: Resources,
        resourceId: Int,
        prompt: String
    ) {
        // Verificação crítica de autenticação
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        if (user == null) {
            android.util.Log.e("BakingViewModel", "Tentativa de usar IA sem autenticação")
            _uiState.value = UiState.Error("Usuário deve estar autenticado para usar a IA")
            return
        }
        
        _uiState.value = UiState.Loading

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Decodificar bitmap em background thread
                val bitmap = BitmapFactory.decodeResource(resources, resourceId)
                
                val response = generativeModel.generateContent(
                    content {
                        image(bitmap)
                        text(prompt)
                    }
                )
                response.text?.let { outputContent ->
                    _uiState.value = UiState.Success(outputContent)
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.localizedMessage ?: "")
            }
        }
    }
}