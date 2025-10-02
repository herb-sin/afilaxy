package com.afilaxy.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afilaxy.security.InputValidator
import com.afilaxy.utils.ErrorHandler
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isRegisterMode: Boolean = false,
    val showRegistrationSuccess: Boolean = false,
    val passwordVisible: Boolean = false,
    val errorMessage: String? = null
)

class LoginViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
    
    private val firebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    
    fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(email = email)
    }
    
    fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(password = password)
    }
    
    fun togglePasswordVisibility() {
        _uiState.value = _uiState.value.copy(passwordVisible = !_uiState.value.passwordVisible)
    }
    
    fun toggleMode() {
        _uiState.value = _uiState.value.copy(
            isRegisterMode = !_uiState.value.isRegisterMode,
            errorMessage = null
        )
    }
    
    fun login(onSuccess: () -> Unit) {
        val currentState = _uiState.value
        
        if (!validateInputs()) return
        
        _uiState.value = currentState.copy(isLoading = true, errorMessage = null)
        
        viewModelScope.launch {
            // Timeout de segurança
            launch {
                delay(15000)
                if (_uiState.value.isLoading) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Timeout: Problemas de conectividade. Tente novamente."
                    )
                }
            }
            
            ErrorHandler.safeSuspendCall(
                operation = "login",
                onError = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.userMessage
                    )
                }
            ) {
                firebaseAuth.signInWithEmailAndPassword(currentState.email, currentState.password)
                    .addOnCompleteListener { task ->
                        if (!_uiState.value.isLoading) return@addOnCompleteListener
                        
                        _uiState.value = _uiState.value.copy(isLoading = false)
                        
                        if (task.isSuccessful) {
                            val user = firebaseAuth.currentUser
                            if (user?.isEmailVerified == true) {
                                onSuccess()
                            } else {
                                _uiState.value = _uiState.value.copy(
                                    errorMessage = "Confirme seu e-mail antes de acessar."
                                )
                            }
                        } else {
                            val errorResult = ErrorHandler.handleError(
                                task.exception ?: Exception("Erro desconhecido"),
                                "login"
                            )
                            _uiState.value = _uiState.value.copy(errorMessage = errorResult.userMessage)
                        }
                    }
            }
        }
    }
    
    fun register() {
        val currentState = _uiState.value
        
        if (!validateInputs()) return
        
        _uiState.value = currentState.copy(isLoading = true, errorMessage = null)
        
        viewModelScope.launch {
            // Timeout de segurança
            launch {
                delay(10000)
                if (_uiState.value.isLoading) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Timeout: Verifique sua conexão com a internet"
                    )
                }
            }
            
            ErrorHandler.safeSuspendCall(
                operation = "register",
                onError = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.userMessage
                    )
                }
            ) {
                firebaseAuth.createUserWithEmailAndPassword(currentState.email, currentState.password)
                    .addOnCompleteListener { task ->
                        _uiState.value = _uiState.value.copy(isLoading = false)
                        
                        if (task.isSuccessful) {
                            val user = firebaseAuth.currentUser
                            firebaseAuth.setLanguageCode("pt")
                            user?.sendEmailVerification()
                            
                            createUserProfile(user?.uid, user?.email)
                            
                            _uiState.value = _uiState.value.copy(showRegistrationSuccess = true)
                        } else {
                            val errorResult = ErrorHandler.handleError(
                                task.exception ?: Exception("Erro desconhecido"),
                                "register"
                            )
                            _uiState.value = _uiState.value.copy(errorMessage = errorResult.userMessage)
                        }
                    }
            }
        }
    }
    
    fun resetPassword() {
        val email = _uiState.value.email
        
        if (email.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Informe um e-mail válido para recuperar a senha."
            )
            return
        }
        
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        
        firebaseAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                _uiState.value = _uiState.value.copy(isLoading = false)
                
                if (task.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "E-mail de recuperação enviado! Verifique sua caixa de SPAM!"
                    )
                } else {
                    val errorResult = ErrorHandler.handleError(
                        task.exception ?: Exception("Erro desconhecido"),
                        "resetPassword"
                    )
                    _uiState.value = _uiState.value.copy(errorMessage = errorResult.userMessage)
                }
            }
    }
    
    fun dismissRegistrationSuccess() {
        _uiState.value = _uiState.value.copy(
            showRegistrationSuccess = false,
            isRegisterMode = false
        )
    }
    
    private fun validateInputs(): Boolean {
        val currentState = _uiState.value
        
        val emailValidation = InputValidator.validateEmail(currentState.email)
        val passwordValidation = InputValidator.validatePassword(currentState.password)
        
        return when {
            !emailValidation.isValid -> {
                _uiState.value = currentState.copy(errorMessage = emailValidation.errorMessage)
                false
            }
            !passwordValidation.isValid -> {
                _uiState.value = currentState.copy(errorMessage = passwordValidation.errorMessage)
                false
            }
            else -> true
        }
    }
    
    private fun createUserProfile(uid: String?, email: String?) {
        if (uid == null || email == null) return
        
        val userData = mapOf(
            "name" to email,
            "email" to email,
            "isHelper" to true,
            "createdAt" to System.currentTimeMillis()
        )
        
        firebaseFirestore.collection("users")
            .document(uid)
            .set(userData)
            .addOnFailureListener { e ->
                android.util.Log.e("LoginViewModel", "Erro ao criar perfil: ${e.message}")
            }
    }
}