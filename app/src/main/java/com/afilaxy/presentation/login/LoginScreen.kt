package com.afilaxy.presentation.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.afilaxy.R
import com.afilaxy.saveUserLocation

fun translateFirebaseError(error: String): String {
    return when {
        error.contains("The email address is badly formatted") -> "Formato de e-mail inválido"
        error.contains("Password should be at least 6 characters") -> "A senha deve ter pelo menos 6 caracteres"
        error.contains("The password is invalid") -> "Senha inválida"
        error.contains("There is no user record") -> "Usuário não encontrado"
        error.contains("The email address is already in use") -> "E-mail já cadastrado"
        error.contains("A network error") -> "Erro de rede"
        error.contains("An internal error has occurred") -> "Erro interno do servidor"
        else -> error
    }
}

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    LaunchedEffect(Unit) {
        // Salvar localização quando necessário
    }

    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LoginHeader(isRegisterMode = uiState.isRegisterMode)
        
        LoginForm(
            email = uiState.email,
            password = uiState.password,
            passwordVisible = uiState.passwordVisible,
            onEmailChange = viewModel::updateEmail,
            onPasswordChange = viewModel::updatePassword,
            onPasswordVisibilityToggle = viewModel::togglePasswordVisibility
        )
        Spacer(modifier = Modifier.height(16.dp))

        LoginActions(
            isRegisterMode = uiState.isRegisterMode,
            isLoading = uiState.isLoading,
            onLoginClick = { 
                viewModel.login()
                saveUserLocation(context)
                onLoginSuccess()
            },
            onRegisterClick = viewModel::register,
            onModeToggle = viewModel::toggleMode,
            onPasswordReset = viewModel::resetPassword
        )

        
        uiState.errorMessage?.let { message ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = message, color = MaterialTheme.colorScheme.error)
        }
        
        // Debug: verificar estado do card
        LaunchedEffect(uiState.showRegistrationSuccess) {
            android.util.Log.d("LoginScreen", "showRegistrationSuccess = ${uiState.showRegistrationSuccess.toString().replace("\n", "").replace("\r", "")}") 
        }
        
        if (uiState.showRegistrationSuccess) {
            android.util.Log.d("LoginScreen", "Renderizando RegistrationSuccessCard")
            RegistrationSuccessCard(onDismiss = viewModel::dismissRegistrationSuccess)
        }
    }  
}