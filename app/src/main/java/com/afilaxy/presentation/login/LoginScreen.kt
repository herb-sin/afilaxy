package com.afilaxy.presentation.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.afilaxy.R
import com.afilaxy.saveUserLocation
import android.util.Log
import kotlinx.coroutines.delay
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    var isRegisterMode by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

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
        
        // INSTRUÇÕES PARA EMULADOR
        Text(
            text = if (isRegisterMode) 
                "Para teste no emulador use:\nEmail: teste@emulador.com\nSenha: 123456" 
            else 
                "Emulador: Use qualquer email válido\nSe travar, aguarde 15s para bypass",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
        )
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
                
                // BYPASS TEMPORÁRIO PARA EMULADOR
                if (email == "teste@emulador.com" && password == "123456") {
                    Log.d("LoginScreen", "Usando bypass do emulador")
                    loading = false
                    onLoginSuccess()
                    return@Button
                }
                
                // BYPASS PARA CONTAS REAIS NO EMULADOR (sem verificação de email)
                if (email.contains("@") && password.length >= 6) {
                    Log.d("LoginScreen", "Tentando bypass para conta real no emulador")
                    
                    coroutineScope.launch {
                        delay(2000) // Simular delay de rede
                        if (loading) {
                            Log.d("LoginScreen", "Executando bypass após timeout")
                            loading = false
                            onLoginSuccess()
                        }
                    }
                }
                
                if (isRegisterMode) {
                    Log.d("LoginScreen", "Iniciando cadastro para: $email")
                    
                    // Timeout de segurança
                    coroutineScope.launch {
                        delay(10000) // 10 segundos
                        if (loading) {
                            loading = false
                            errorMessage = "Timeout: Verifique sua conexão com a internet"
                            Log.e("LoginScreen", "Timeout no cadastro")
                        }
                    }
                    
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            Log.d("LoginScreen", "Callback executado - Sucesso: ${task.isSuccessful}")
                            loading = false
                            if (task.isSuccessful) {
                                val user = auth.currentUser
                                Log.d("LoginScreen", "Usuário criado: ${user?.uid}")
                                auth.setLanguageCode("pt")
                                user?.sendEmailVerification()
                                
                                // Criar perfil no Firestore como helper por padrão
                                user?.let { firebaseUser ->
                                    val firestore = FirebaseFirestore.getInstance()
                                    val userData = mapOf<String, Any>(
                                        "name" to (firebaseUser.email ?: "Usuário"),
                                        "email" to (firebaseUser.email ?: "usuario@exemplo.com"),
                                        "isHelper" to true, // Novo usuário é helper por padrão
                                        "createdAt" to System.currentTimeMillis()
                                    )
                                    
                                    firestore.collection("users")
                                        .document(firebaseUser.uid)
                                        .set(userData)
                                        .addOnSuccessListener {
                                            Log.d("LoginScreen", "Perfil criado no Firestore")
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e("LoginScreen", "Erro ao criar perfil: ${e.message}")
                                        }
                                }
                                
                                // Salvar localização após criar perfil
                                coroutineScope.launch {
                                    delay(1000) // Aguardar perfil ser criado
                                    saveUserLocation(context)
                                }
                                
                                onLoginSuccess()
                            } else {
                                val error = task.exception
                                Log.e("LoginScreen", "Erro no cadastro: ${error?.message}")
                                errorMessage = when {
                                    error?.message?.contains("The email address is already in use") == true ||
                                    error?.message?.contains("email address is already") == true ->
                                    "Este e-mail já está cadastrado. Faça login ou recupere sua senha."
                                    error?.message?.contains("network") == true ||
                                    error?.message?.contains("timeout") == true ->
                                    "Problema de rede. Verifique sua conexão e tente novamente."
                                    error?.message?.contains("SERVICE_NOT_AVAILABLE") == true ->
                                    "Firebase indisponível. Tente novamente em alguns minutos."
                                    else -> "Erro: ${error?.localizedMessage ?: error?.message ?: "Desconhecido"}"
                                }
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.e("LoginScreen", "Falha imediata: ${exception.message}")
                            loading = false
                            errorMessage = "Erro de conexão: ${exception.message}"
                        }
                } else {
                    Log.d("LoginScreen", "Iniciando login para: $email")
                    
                    // Timeout de segurança para emulador
                    coroutineScope.launch {
                        delay(15000) // 15 segundos
                        if (loading) {
                            loading = false
                            errorMessage = "Timeout: Problemas de conectividade. Tente novamente ou use o bypass para teste."
                            Log.e("LoginScreen", "Timeout no login")
                        }
                    }
                    
                    auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (!loading) return@addOnCompleteListener // Já teve timeout
                        
                        loading = false
                        Log.d("LoginScreen", "Login callback - Sucesso: ${task.isSuccessful}")
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            Log.d("LoginScreen", "Usuário logado: ${user?.uid}, Email verificado: ${user?.isEmailVerified}")
                            if (user?.isEmailVerified == true) {
                                // Salvar localização no login
                                saveUserLocation(context)
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
                                error?.message?.contains("network") == true ||
                                error?.message?.contains("timeout") == true ->
                                    "Problema de rede. Verifique sua conexão e tente novamente."
                                error?.message?.contains("SERVICE_NOT_AVAILABLE") == true ->
                                    "Firebase indisponível. Tente novamente em alguns minutos."
                                else -> "Erro: ${error?.localizedMessage ?: error?.message ?: "Desconhecido"}"
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
        
        // Botão de bypass para desenvolvimento/teste
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(
            onClick = {
                Log.d("LoginScreen", "Executando bypass de desenvolvimento")
                // Simular usuário autenticado para teste
                saveUserLocation(context)
                onLoginSuccess()
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFFFF9800)
            )
        ) {
            Text("BYPASS - Modo Teste")
        }
    }  
}