package com.afilaxy.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afilaxy.security.UnifiedValidator
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
            try {
                firebaseAuth.signInWithEmailAndPassword(currentState.email, currentState.password)
                    .addOnCompleteListener { task ->
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
                            _uiState.value = _uiState.value.copy(errorMessage = "Erro no login")
                        }
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Erro no login"
                )
            }
        }
    }
    
    fun register() {
        val currentState = _uiState.value
        
        if (!validateInputs()) return
        
        _uiState.value = currentState.copy(isLoading = true, errorMessage = null)
        
        viewModelScope.launch {
            try {
                firebaseAuth.createUserWithEmailAndPassword(currentState.email, currentState.password)
                    .addOnCompleteListener { task ->
                        _uiState.value = _uiState.value.copy(isLoading = false)
                        
                        if (task.isSuccessful) {
                            val user = firebaseAuth.currentUser
                            firebaseAuth.setLanguageCode("pt")
                            user?.sendEmailVerification()
                            
                            createUserProfile(user?.uid, user?.email)
                            
                            android.util.Log.d("LoginViewModel", "✅ Cadastro realizado com sucesso - mostrando card")
                            _uiState.value = _uiState.value.copy(showRegistrationSuccess = true)
                        } else {
                            android.util.Log.e("LoginViewModel", "❌ Erro no cadastro: ${task.exception?.message}")
                            _uiState.value = _uiState.value.copy(errorMessage = "Erro no cadastro: ${task.exception?.localizedMessage}")
                        }
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Erro no cadastro"
                )
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
                    _uiState.value = _uiState.value.copy(errorMessage = "Erro na recuperação de senha")
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
        
        val emailValidation = UnifiedValidator.validateEmail(currentState.email)
        val passwordValidation = UnifiedValidator.validatePassword(currentState.password)
        
        return when {
            emailValidation is UnifiedValidator.ValidationResult.Invalid -> {
                _uiState.value = currentState.copy(errorMessage = emailValidation.errors.first())
                false
            }
            passwordValidation is UnifiedValidator.ValidationResult.Invalid -> {
                _uiState.value = currentState.copy(errorMessage = passwordValidation.errors.first())
                false
            }
            else -> true
        }
    }
    
    private fun createUserProfile(uid: String?, email: String?) {
        if (uid == null || email == null) return
        
        // Verify user is authenticated before creating profile
        val currentUser = firebaseAuth.currentUser
        if (currentUser == null || currentUser.uid != uid) {
            android.util.Log.w("LoginViewModel", "User not authenticated for profile creation")
            return
        }
        
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
                android.util.Log.e("LoginViewModel", "Erro ao criar perfil")
            }
    }
}