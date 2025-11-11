package com.afilaxy.presentation.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailVerificationScreen(
    onVerified: () -> Unit,
    onLogout: () -> Unit
) {
    var isChecking by remember { mutableStateOf(false) }
    var showResendSuccess by remember { mutableStateOf(false) }
    
    val currentUser = FirebaseAuth.getInstance().currentUser
    
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(3000)
            currentUser?.reload()?.addOnCompleteListener {
                if (currentUser.isEmailVerified) {
                    onVerified()
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Verificação de Email") },
                actions = {
                    TextButton(onClick = onLogout) {
                        Text("Sair")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Email,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Verifique seu email",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Enviamos um link de verificação para:",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = currentUser?.email ?: "",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Clique no link do email para verificar sua conta.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "💡 Não encontrou? Verifique sua caixa de spam.",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = {
                    isChecking = true
                    currentUser?.reload()?.addOnCompleteListener {
                        isChecking = false
                        if (currentUser.isEmailVerified) {
                            onVerified()
                        }
                    }
                },
                enabled = !isChecking,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isChecking) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Verificar Agora")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedButton(
                onClick = {
                    currentUser?.sendEmailVerification()?.addOnCompleteListener {
                        showResendSuccess = true
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Reenviar Email")
            }
            
            if (showResendSuccess) {
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(3000)
                    showResendSuccess = false
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        text = "Email reenviado!",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}