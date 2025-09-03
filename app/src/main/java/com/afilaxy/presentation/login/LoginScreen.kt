package com.afilaxy.presentation.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import com.google.firebase.auth.FirebaseAuth
import com.afilaxy.R

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    var isRegisterMode by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logotipo
        Image(
            painter = painterResource(id = R.drawable.afilaxy_logo), // Adicione seu logo em res/drawable/afilaxyLogo.png
            contentDescription = "Logo Afilaxy",
            modifier = Modifier
                .height(180.dp)
                .padding(bottom = 16.dp)
        )

        Text(text = if (isRegisterMode) "Cadastro" else "Login", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("E-mail") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Senha") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                loading = true
                errorMessage = null
                val auth = FirebaseAuth.getInstance()
                if (isRegisterMode) {
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            loading = false
                            if (task.isSuccessful) {
                                val user = auth.currentUser
                                auth.setLanguageCode("pt") // Define idioma para português
                                user?.sendEmailVerification() // Envia e-mail de verificação
                                onLoginSuccess()
                            } else {
                                val error = task.exception
                                errorMessage = when {
                                    error?.message?.contains("The email address is already in use") == true ||
                                    error?.message?.contains("email address is already") == true ->
                                    "Este e-mail já está cadastrado. Faça login ou recupere sua senha."
                                    else -> error?.localizedMessage ?: "Erro ao cadastrar"
                                }
                            }
                        }
                } else {
                    auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        loading = false
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            if (user?.isEmailVerified == true) {
                                onLoginSuccess()
                                } else {
                                    errorMessage = "Confirme seu e-mail antes de acessar."
                                }
                        } else {
                            val error = task.exception
                            errorMessage = when {
                                error?.message?.contains("The supplied auth credential is incorrect") == true ||
                                error?.message?.contains("no user record") == true ||
                                error?.message?.contains("There is no user record") == true -> 
                                    "E-mail ou senha incorretos, ou usuário não cadastrado."
                                else -> error?.localizedMessage ?: "Erro ao autenticar"
                            }
                        }
                    }
                }
            },
            enabled = !loading,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isRegisterMode) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
            )
        ) {
            Text(if (loading) "Processando..." else if (isRegisterMode) "Cadastrar" else "Entrar")
        }

        Spacer(modifier = Modifier.height(8.dp))
        TextButton(
            onClick = { isRegisterMode = !isRegisterMode }
        ) {
            Text(if (isRegisterMode) "Já tem conta? Fazer login" else "Não tem conta? Cadastre-se")
        }

        if (!isRegisterMode) {
            TextButton(
                onClick = {
                    if (email.isBlank()) {
                        errorMessage = "Informe um e-mail válido para recuperar a senha."
                        return@TextButton
                    }
                    loading = true
                    errorMessage = null
                    val auth = FirebaseAuth.getInstance()
                    auth.sendPasswordResetEmail(email)
                        .addOnCompleteListener { task ->
                            loading = false
                            if (task.isSuccessful) {
                                errorMessage = "E-mail de recuperação enviado! Verifique sua caixa de SPAM!"
                            } else {
                                errorMessage = task.exception?.localizedMessage ?: "Erro ao enviar recuperação"
                            }
                        }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Esqueci a senha")
            }
        }

        errorMessage?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }
    }  
}