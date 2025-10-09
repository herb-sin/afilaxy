package com.afilaxy.presentation.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.afilaxy.security.InputValidator
import com.afilaxy.security.AuthGuard
import com.afilaxy.utils.ErrorHandler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    var userName by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    
    // Instâncias Firebase otimizadas com lazy loading
    val auth = remember { FirebaseAuth.getInstance() }
    val firestore = remember { FirebaseFirestore.getInstance() }
    val currentUser = auth.currentUser
    
    LaunchedEffect(Unit) {
        if (currentUser == null) {
            isLoading = false
            message = "Usuário não autenticado"
            return@LaunchedEffect
        }
        
        currentUser.let { user ->
            firestore.collection("users")
                .document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    userName = document.getString("name") ?: user.email ?: ""
                    isLoading = false
                }
                .addOnFailureListener {
                    userName = user.email ?: ""
                    isLoading = false
                }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.Top
        ) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Text("Editar Perfil", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(32.dp))
                
                OutlinedTextField(
                    value = userName,
                    onValueChange = { newValue ->
                        // Sanitizar entrada em tempo real
                        val sanitized = newValue.filter { it.isLetter() || it.isWhitespace() }
                        if (sanitized.length <= 50) {
                            userName = sanitized
                        }
                    },
                    label = { Text("Nome") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSaving,
                    supportingText = {
                        Text("${userName.length}/50 caracteres")
                    }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = currentUser?.email ?: "",
                    onValueChange = { },
                    label = { Text("E-mail") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = {
                        // Validar entrada
                        val nameValidation = InputValidator.validateName(userName)
                        if (!nameValidation.isValid) {
                            message = nameValidation.errorMessage
                            return@Button
                        }
                        
                        // Verificar se usuário está logado
                        if (currentUser == null) {
                            message = "Usuário não autenticado"
                            return@Button
                        }
                        
                        isSaving = true
                        message = null
                        

                        
                                        ErrorHandler.safeCall(
                            operation = "updateUserProfile",
                            onError = { error ->
                                isSaving = false
                                message = error.userMessage
                            }
                        ) {
                            currentUser?.let { user ->
                                firestore.collection("users")
                                    .document(user.uid)
                                    .update("name", userName)
                                    .addOnSuccessListener {
                                        isSaving = false
                                        message = "Nome atualizado com sucesso!"
                                    }
                                    .addOnFailureListener { e ->
                                        val errorResult = ErrorHandler.handleError(e, "updateProfile")
                                        isSaving = false
                                        message = errorResult.userMessage
                                    }
                            } ?: run {
                                isSaving = false
                                message = "Usuário não autenticado"
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    } else {
                        Text("Salvar")
                    }
                }
                
                message?.let { msg ->
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = msg,
                        color = if (msg.contains("sucesso")) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}