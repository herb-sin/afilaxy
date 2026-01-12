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
import androidx.hilt.navigation.compose.hiltViewModel
import com.afilaxy.R
import com.afilaxy.utils.LocationUtils
import com.afilaxy.security.SecureLogger



@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
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
                viewModel.login { success ->
                    if (success) {
                        // Obter localização após login bem-sucedido
                        LocationUtils.getCurrentLocation(context) { lat, lng ->
                            // Localização obtida: $lat, $lng
                        }
                        onLoginSuccess()
                    }
                }
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
            if (uiState.showRegistrationSuccess) {
                SecureLogger.d("LoginScreen", "Registration success card triggered")
            }
        }
        
        if (uiState.showRegistrationSuccess) {
            SecureLogger.d("LoginScreen", "Renderizando RegistrationSuccessCard")
            RegistrationSuccessCard(onDismiss = viewModel::dismissRegistrationSuccess)
        }
    }  
}