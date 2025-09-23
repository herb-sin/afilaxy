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
    
    fun loadUserData() {
        Log.d("HomeViewModel", "Iniciando carregamento de dados do usuário")
        _uiState.value = _uiState.value.copy(isLoading = true)
        
        viewModelScope.launch {
            try {
                val auth = FirebaseAuth.getInstance()
                val firestore = FirebaseFirestore.getInstance()
                val user = auth.currentUser
                
                Log.d("HomeViewModel", "Usuário atual: ${user?.uid}")
                
                if (user != null) {
                    Log.d("HomeViewModel", "Buscando documento do usuário no Firestore")
                    val userDoc = firestore.collection("users")
                        .document(user.uid)
                        .get()
                        .await()
                    
                    Log.d("HomeViewModel", "Documento existe: ${userDoc.exists()}")
                    
                    if (!userDoc.exists()) {
                        // Criar perfil se não existir
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
                    }
                    
                    // TODO: Solicitar localização atual e salvar
                    // Isso deve ser feito na MainActivity com permissões adequadas
                    
                    val isHelper = userDoc.getBoolean("isHelper") ?: true
                    val userName = userDoc.getString("name") ?: user.email ?: "Usuário"
                    
                    Log.d("HomeViewModel", "Dados carregados - Nome: $userName, Helper: $isHelper")
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        userName = userName,
                        isHelper = isHelper
                    )
                } else {
                    Log.e("HomeViewModel", "Usuário não autenticado - criando perfil de teste")
                    // Criar perfil simulado no Firestore para teste
                    try {
                        val firestore = FirebaseFirestore.getInstance()
                        val testUserId = "test_user_emulator_${System.currentTimeMillis()}"
                        val userData = mapOf<String, Any>(
                            "name" to "Usuário Teste (Emulador)",
                            "email" to "teste@emulador.com",
                            "isHelper" to true,
                            "location" to com.google.firebase.firestore.GeoPoint(-23.6209, -46.6707), // Localização do emulador
                            "createdAt" to System.currentTimeMillis()
                        )
                        
                        firestore.collection("users")
                            .document(testUserId)
                            .set(userData)
                            .await()
                        
                        Log.d("HomeViewModel", "Perfil de teste criado: $testUserId")
                    } catch (e: Exception) {
                        Log.e("HomeViewModel", "Erro ao criar perfil de teste: ${e.message}")
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        userName = "Usuário Teste (Emulador)",
                        isHelper = true,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Erro ao carregar dados: ${e.message}", e)
                
                // Fallback: usar dados locais se Firestore falhar
                val auth = FirebaseAuth.getInstance()
                val user = auth.currentUser
                
                // Criar perfil simulado se necessário
                try {
                    val firestore = FirebaseFirestore.getInstance()
                    val userId = user?.uid ?: "fallback_user_${System.currentTimeMillis()}"
                    val userData = mapOf<String, Any>(
                        "name" to (user?.email?.substringBefore("@") ?: "Usuário Fallback"),
                        "email" to (user?.email ?: "fallback@teste.com"),
                        "isHelper" to true,
                        "location" to com.google.firebase.firestore.GeoPoint(-23.6209, -46.6707),
                        "createdAt" to System.currentTimeMillis()
                    )
                    
                    firestore.collection("users")
                        .document(userId)
                        .set(userData)
                        .await()
                    
                    Log.d("HomeViewModel", "Perfil fallback criado: $userId")
                } catch (fallbackError: Exception) {
                    Log.e("HomeViewModel", "Erro ao criar perfil fallback: ${fallbackError.message}")
                }
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    userName = user?.email ?: "Usuário Teste (Emulador)",
                    isHelper = true,
                    errorMessage = null
                )
                
                Log.d("HomeViewModel", "Usando fallback offline")
            }
        }
    }
    
    fun toggleHelperStatus() {
        val currentState = _uiState.value
        val newHelperStatus = !currentState.isHelper
        
        Log.d("HomeViewModel", "Alterando status de helper para: $newHelperStatus")
        
        viewModelScope.launch {
            try {
                val auth = FirebaseAuth.getInstance()
                val firestore = FirebaseFirestore.getInstance()
                val user = auth.currentUser
                
                if (user != null) {
                    firestore.collection("users")
                        .document(user.uid)
                        .update("isHelper", newHelperStatus)
                        .await()
                    
                    Log.d("HomeViewModel", "Status atualizado com sucesso")
                    _uiState.value = _uiState.value.copy(isHelper = newHelperStatus)
                } else {
                    // Modo teste: apenas atualizar localmente
                    Log.d("HomeViewModel", "Atualizando status em modo teste")
                    _uiState.value = _uiState.value.copy(isHelper = newHelperStatus)
                }
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
                val auth = FirebaseAuth.getInstance()
                val firestore = FirebaseFirestore.getInstance()
                val currentUser = auth.currentUser
                
                android.util.Log.d("HomeViewModel", "👤 Usuário atual: ${currentUser?.uid} (${currentUser?.email})")
                
                if (currentUser == null) {
                    android.util.Log.e("HomeViewModel", "❌ 🚨 USUÁRIO NÃO AUTENTICADO!")
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Erro: Usuário não autenticado"
                    )
                    return@launch
                }
                
                android.util.Log.d("HomeViewModel", "🔍 Buscando helpers no Firestore...")
                
                // Buscar todos os helpers exceto o usuário atual
                val usersSnapshot = firestore.collection("users")
                    .whereEqualTo("isHelper", true)
                    .get()
                    .await()
                
                android.util.Log.d("HomeViewModel", "👥 ✅ ENCONTRADOS ${usersSnapshot.documents.size} USUÁRIOS HELPERS")
                
                var notificationsSent = 0
                
                for (document in usersSnapshot.documents) {
                    val helperEmail = document.getString("email") ?: "Helper"
                    android.util.Log.d("HomeViewModel", "🔍 Verificando: ${document.id} ($helperEmail)")
                    
                    if (document.id == currentUser.uid) {
                        android.util.Log.d("HomeViewModel", "⏭️ Pulando próprio usuário")
                        continue
                    }
                    
                    android.util.Log.d("HomeViewModel", "📤 🔥 ENVIANDO PARA: ${document.id} ($helperEmail)")
                    
                    val alertData = mapOf(
                        "type" to "emergency_alert",
                        "emergencyId" to "test_${System.currentTimeMillis()}",
                        "requesterName" to (currentUser.email ?: "Teste"),
                        "location" to com.google.firebase.firestore.GeoPoint(-23.6200, -46.6700),
                        "timestamp" to System.currentTimeMillis()
                    )
                    
                    android.util.Log.d("HomeViewModel", "📝 Dados da notificação: $alertData")
                    
                    val docRef = firestore
                        .collection("users")
                        .document(document.id)
                        .collection("notifications")
                        .add(alertData)
                        .await()
                    
                    notificationsSent++
                    android.util.Log.d("HomeViewModel", "✅ 🎉 NOTIFICAÇÃO ENVIADA! Doc: ${docRef.id}")
                    android.util.Log.d("HomeViewModel", "📍 Caminho: users/${document.id}/notifications/${docRef.id}")
                }
                
                android.util.Log.d("HomeViewModel", "🏁 ✅ TESTE CONCLUÍDO! $notificationsSent notificações enviadas")
                
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Teste: $notificationsSent notificações enviadas"
                )
                
            } catch (e: Exception) {
                android.util.Log.e("HomeViewModel", "❌ 🚨 ERRO GRAVE NO TESTE: ${e.message}")
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Erro no teste: ${e.message}"
                )
            }
        }
    }
}