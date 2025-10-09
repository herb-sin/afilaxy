package com.afilaxy.presentation.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.afilaxy.R

@Composable
fun LoginHeader(isRegisterMode: Boolean) {
    Image(
        painter = painterResource(id = R.drawable.afilaxy_logo),
        contentDescription = "Logo Afilaxy",
        modifier = Modifier
            .height(180.dp)
            .padding(bottom = 16.dp)
    )
    
    Text(
        text = if (isRegisterMode) "Cadastro" else "Login",
        style = MaterialTheme.typography.headlineMedium
    )
    Spacer(modifier = Modifier.height(32.dp))
}

@Composable
fun LoginForm(
    email: String,
    password: String,
    passwordVisible: Boolean,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordVisibilityToggle: () -> Unit
) {
    OutlinedTextField(
        value = email,
        onValueChange = onEmailChange,
        label = { Text("E-mail") },
        modifier = Modifier.fillMaxWidth()
    )
    
    Spacer(modifier = Modifier.height(8.dp))
    
    OutlinedTextField(
        value = password,
        onValueChange = onPasswordChange,
        label = { Text("Senha") },
        modifier = Modifier.fillMaxWidth(),
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            TextButton(onClick = onPasswordVisibilityToggle) {
                Text(
                    text = if (passwordVisible) "👁️" else "🙈",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    )
    
    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
fun LoginActions(
    isRegisterMode: Boolean,
    isLoading: Boolean,
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onModeToggle: () -> Unit,
    onPasswordReset: () -> Unit
) {
    Button(
        onClick = if (isRegisterMode) onRegisterClick else onLoginClick,
        enabled = !isLoading,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isRegisterMode) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
        )
    ) {
        Text(
            when {
                isLoading -> "Processando..."
                isRegisterMode -> "Cadastrar"
                else -> "Entrar"
            }
        )
    }
    
    Spacer(modifier = Modifier.height(8.dp))
    
    TextButton(onClick = onModeToggle) {
        Text(
            if (isRegisterMode) "Já tem conta? Fazer login" 
            else "Não tem conta? Cadastre-se"
        )
    }
    
    if (!isRegisterMode) {
        TextButton(
            onClick = onPasswordReset,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Esqueci a senha")
        }
    }
}

@Composable
fun RegistrationSuccessCard(onDismiss: () -> Unit) {
    android.util.Log.d("RegistrationSuccessCard", "🟢 Card sendo renderizado!")
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF4CAF50)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "✅ Conta criada com sucesso!",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Verifique sua caixa de entrada (e SPAM) para confirmar seu e-mail. Após confirmar, faça logout e login novamente para atualizar sua localização.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            TextButton(onClick = onDismiss) {
                Text("Entendi, fazer login")
            }
        }
    }
}