package com.afilaxy.presentation.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.afilaxy.security.AuthGuard
import com.afilaxy.security.InputSanitizer
import com.google.firebase.firestore.GeoPoint
import com.afilaxy.notification.FCMTokenManager

class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    // Instâncias Firebase reutilizáveis
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    
    fun loadUserData() {
        Log.d("HomeViewModel", "Iniciando carregamento de dados do usuário")
        _uiState.value = _uiState.value.copy(isLoading = true)
        
        viewModelScope.launch {
            try {
                val user = auth.currentUser
                
                Log.d("HomeViewModel", "Usuário atual autenticado")
                
                if (user != null) {
                    Log.d("HomeViewModel", "Buscando documento do usuário no Firestore")
                    val userDoc = firestore.collection("users")
                        .document(user.uid)
                        .get()
                        .await()
                    
                    Log.d("HomeViewModel", "Documento existe: ${userDoc.exists()}")
                    
                    val isHelper: Boolean
                    val userName: String
                    
                    if (!userDoc.exists()) {
                        Log.d("HomeViewModel", "Criando perfil no Firestore")
                        val userData = mapOf<String, Any>(
                            "name" to (user.email ?: "Usuário"),
                            "email" to (user.email ?: "usuario@exemplo.com"),
                            "isHelper" to true,
                            "createdAt" to System.currentTimeMillis()
                        )
                        
                        firestore.collection("users")
                            .document(user.uid)
                            .set(userData)
                            .await()
                        
                        Log.d("HomeViewModel", "Perfil criado com sucesso")
                        
                        isHelper = true
                        userName = user.email ?: "Usuário"
                    } else {
                        isHelper = userDoc.getBoolean("isHelper") ?: true
                        userName = userDoc.getString("name") ?: user.email ?: "Usuário"
                    }
                    
                    Log.d("HomeViewModel", "Dados carregados com sucesso")
                    
                    // Atualizar token FCM
                    FCMTokenManager.updateFCMToken()
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        userName = userName,
                        isHelper = isHelper
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Usuário não autenticado"
                    )
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Erro ao carregar dados: ${e.message}", e)
                
                val user = auth.currentUser
                
                // Fallback para dados offline
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    userName = user?.email ?: "Usuário",
                    isHelper = true,
                    errorMessage = null
                )
            }
        }
    }
    
    fun toggleHelperStatus() {
        val currentState = _uiState.value
        val newHelperStatus = !currentState.isHelper
        
        Log.d("HomeViewModel", "Alterando status de helper: $newHelperStatus")
        
        // Atualizar UI imediatamente para responsividade
        _uiState.value = _uiState.value.copy(isHelper = newHelperStatus)
        
        viewModelScope.launch {
            try {
                val user = auth.currentUser
                if (user != null) {
                    firestore.collection("users")
                        .document(user.uid)
                        .update("isHelper", newHelperStatus)
                        .await()
                    
                    Log.d("HomeViewModel", "✅ Status atualizado no Firestore")
                } else {
                    Log.w("HomeViewModel", "Usuário não autenticado - mantendo apenas localmente")
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "❌ Erro ao salvar no Firestore: ${e.message}")
                Log.w("HomeViewModel", "Mantendo alteração apenas localmente")
                // Manter a mudança local mesmo se Firestore falhar
            }
        }
    }
    
    fun updateUserLocation(latitude: Double, longitude: Double) {
        Log.d("HomeViewModel", "Atualizando localização do usuário: $latitude, $longitude")
        
        viewModelScope.launch {
            try {
                val user = auth.currentUser
                if (user != null) {
                    val location = GeoPoint(latitude, longitude)
                    
                    firestore.collection("users")
                        .document(user.uid)
                        .update(mapOf(
                            "location" to location,
                            "lastLocationUpdate" to System.currentTimeMillis()
                        ))
                        .await()
                    
                    Log.d("HomeViewModel", "✅ Localização atualizada no Firestore")
                } else {
                    Log.w("HomeViewModel", "Usuário não autenticado - não é possível salvar localização")
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "❌ Erro ao salvar localização: ${e.message}")
            }
        }
    }
    

}