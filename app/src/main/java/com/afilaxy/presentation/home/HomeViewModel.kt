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
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        userName = userName,
                        isHelper = isHelper
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        userName = "Usuário Teste",
                        isHelper = true,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Erro ao carregar dados: ${e.message}", e)
                
                val user = auth.currentUser
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    userName = user?.email ?: "Usuário Teste",
                    isHelper = true,
                    errorMessage = null
                )
            }
        }
    }
    
    fun toggleHelperStatus() {
        val currentState = _uiState.value
        val newHelperStatus = !currentState.isHelper
        
        Log.d("HomeViewModel", "Alterando status de helper")
        
        viewModelScope.launch {
            try {
                val user = auth.currentUser
                
                if (user == null) {
                    Log.e("HomeViewModel", "Tentativa de alterar status de helper sem autenticação")
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Usuário deve estar autenticado para alterar status de helper"
                    )
                    return@launch
                }
                
                if (!user.isEmailVerified) {
                    Log.e("HomeViewModel", "Tentativa de alterar status de helper com email não verificado")
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Email deve estar verificado para alterar status de helper"
                    )
                    return@launch
                }
                
                firestore.collection("users")
                    .document(user.uid)
                    .update("isHelper", newHelperStatus)
                    .await()
                
                Log.d("HomeViewModel", "Status atualizado com sucesso")
                _uiState.value = _uiState.value.copy(isHelper = newHelperStatus)
                
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Erro ao atualizar status: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Erro ao atualizar status: ${e.message}"
                )
            }
        }
    }
    
    fun sendTestNotification() {
        android.util.Log.d("HomeViewModel", "📤 🚨 ===== INICIANDO TESTE DE NOTIFICAÇÃO =====")
        
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser
                
                if (currentUser == null) {
                    android.util.Log.e("HomeViewModel", "❌ 🚨 USUÁRIO NÃO AUTENTICADO!")
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Erro: Usuário não autenticado"
                    )
                    return@launch
                }
                
                val usersSnapshot = firestore.collection("users")
                    .whereEqualTo("isHelper", true)
                    .get()
                    .await()
                
                var notificationsSent = 0
                
                for (document in usersSnapshot.documents) {
                    if (document.id == currentUser.uid) continue
                    
                    val alertData = mapOf(
                        "type" to "emergency_alert",
                        "emergencyId" to "test_${System.currentTimeMillis()}",
                        "requesterName" to (currentUser.email ?: "Teste"),
                        "location" to com.google.firebase.firestore.GeoPoint(-23.6200, -46.6700),
                        "timestamp" to System.currentTimeMillis()
                    )
                    
                    firestore
                        .collection("users")
                        .document(document.id)
                        .collection("notifications")
                        .add(alertData)
                        .await()
                    
                    notificationsSent++
                }
                
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Teste: $notificationsSent notificações enviadas"
                )
                
            } catch (e: Exception) {
                android.util.Log.e("HomeViewModel", "❌ 🚨 ERRO GRAVE NO TESTE: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Erro no teste: ${e.message}"
                )
            }
        }
    }
}