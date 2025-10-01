package com.afilaxy.presentation.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

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
    
    val auth = remember { FirebaseAuth.getInstance() }
    val firestore = remember { FirebaseFirestore.getInstance() }
    val currentUser = auth.currentUser
    
    LaunchedEffect(Unit) {
        currentUser?.let { user ->
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
                    TextButton(onClick = { navController.popBackStack() }) {
                        Text("Voltar")
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
                    onValueChange = { userName = it },
                    label = { Text("Nome") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSaving
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
                        if (userName.isBlank()) {
                            message = "Nome não pode estar vazio"
                            return@Button
                        }
                        
                        isSaving = true
                        message = null
                        
                        currentUser?.let { user ->
                            firestore.collection("users")
                                .document(user.uid)
                                .update("name", userName)
                                .addOnSuccessListener {
                                    isSaving = false
                                    message = "Nome atualizado com sucesso!"
                                }
                                .addOnFailureListener { e ->
                                    isSaving = false
                                    message = "Erro ao atualizar: ${e.message}"
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