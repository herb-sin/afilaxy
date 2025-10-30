package com.afilaxy.presentation.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.afilaxy.notification.NotificationManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

data class HomeUiState(
    val isHelper: Boolean = false,
    val errorMessage: String? = null
)

class HomeViewModel(private val context: Context? = null) : ViewModel() {
    
    var isHelper by mutableStateOf(false)
        private set
        
    var isLoggedIn by mutableStateOf(false)
        private set
        
    var userEmail by mutableStateOf("")
        private set
        
    var statusMessage by mutableStateOf("")
        private set
    
    private val auth = FirebaseAuth.getInstance()
    private val notificationManager = context?.let { NotificationManager(it) }
    
    init {
        // Check auth state without blocking
        auth.currentUser?.let { user ->
            isLoggedIn = true
            userEmail = user.email ?: ""
        }
        
        // Initialize notifications
        viewModelScope.launch {
            notificationManager?.initializeNotifications()
        }
    }
    
    fun toggleHelper() {
        val newHelperStatus = !isHelper
        
        viewModelScope.launch {
            try {
                // For development, simulate success without Firebase
                if (notificationManager != null) {
                    val success = notificationManager.toggleHelperStatus(newHelperStatus)
                    if (success) {
                        isHelper = newHelperStatus
                        statusMessage = if (newHelperStatus) {
                            "Você agora é um helper! Receberá notificações de emergência."
                        } else {
                            "Status de helper desativado."
                        }
                    } else {
                        statusMessage = "Erro ao conectar com Firebase. Modo offline ativado."
                        // Still toggle for demo purposes
                        isHelper = newHelperStatus
                    }
                } else {
                    // Fallback for development without context
                    isHelper = newHelperStatus
                    statusMessage = if (newHelperStatus) {
                        "Helper ativado (modo desenvolvimento)"
                    } else {
                        "Helper desativado (modo desenvolvimento)"
                    }
                }
            } catch (e: Exception) {
                // Fallback - still toggle for demo
                isHelper = newHelperStatus
                statusMessage = "Helper ${if (newHelperStatus) "ativado" else "desativado"} (offline)"
            }
        }
    }
    
    fun quickLogin() {
        // Simple test login - bypass Firebase for test credentials
        isLoggedIn = true
        userEmail = "test@test.com"
    }
    
    fun logout() {
        auth.signOut()
        isLoggedIn = false
        userEmail = ""
        isHelper = false
        statusMessage = "Logout realizado"
    }
    
    fun clearStatusMessage() {
        statusMessage = ""
    }
}