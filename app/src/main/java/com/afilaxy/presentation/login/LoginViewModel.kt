package com.afilaxy.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth
import com.afilaxy.data.NotificationRepository

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,
    val isRegisterMode: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val showRegistrationSuccess: Boolean = false
)

class LoginViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
    
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val notificationRepository = NotificationRepository()
    
    fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(email = email)
    }
    
    fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(password = password)
    }
    
    fun togglePasswordVisibility() {
        _uiState.value = _uiState.value.copy(passwordVisible = !_uiState.value.passwordVisible)
    }
    
    fun login(onResult: (Boolean) -> Unit = {}) {
        if (_uiState.value.email.isBlank() || _uiState.value.password.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Preencha todos os campos")
            return
        }
        

        
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        
        viewModelScope.launch {
            try {
                firebaseAuth.signInWithEmailAndPassword(_uiState.value.email, _uiState.value.password)
                    .addOnCompleteListener { task ->
                        _uiState.value = _uiState.value.copy(isLoading = false)
                        if (task.isSuccessful) {
                            // Save FCM token for notifications
                            firebaseAuth.currentUser?.uid?.let { userId ->
                                viewModelScope.launch {
                                    notificationRepository.saveUserToken(userId)
                                }
                            }
                            onResult(true)
                        } else {
                            val errorMsg = when {
                                task.exception?.message?.contains("credential") == true -> "Email ou senha incorretos"
                                task.exception?.message?.contains("network") == true -> "Erro de conexão"
                                task.exception?.message?.contains("user-not-found") == true -> "Usuário não encontrado"
                                else -> "Erro no login: ${task.exception?.message}"
                            }
                            _uiState.value = _uiState.value.copy(errorMessage = errorMsg)
                            onResult(false)
                        }
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Erro no login"
                )
                onResult(false)
            }
        }
    }
    
    fun register() {
        if (_uiState.value.email.isBlank() || _uiState.value.password.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Preencha todos os campos")
            return
        }
        
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        
        viewModelScope.launch {
            try {
                firebaseAuth.createUserWithEmailAndPassword(_uiState.value.email, _uiState.value.password)
                    .addOnCompleteListener { task ->
                        _uiState.value = _uiState.value.copy(isLoading = false)
                        if (task.isSuccessful) {
                            _uiState.value = _uiState.value.copy(
                                showRegistrationSuccess = true,
                                isRegisterMode = false
                            )
                        } else {
                            _uiState.value = _uiState.value.copy(
                                errorMessage = task.exception?.message ?: "Erro no cadastro"
                            )
                        }
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Erro no cadastro"
                )
            }
        }
    }
    
    fun toggleMode() {
        _uiState.value = _uiState.value.copy(isRegisterMode = !_uiState.value.isRegisterMode)
    }
    
    fun resetPassword() {
        // Reset password logic
    }
    
    fun dismissRegistrationSuccess() {
        _uiState.value = _uiState.value.copy(showRegistrationSuccess = false)
    }
}