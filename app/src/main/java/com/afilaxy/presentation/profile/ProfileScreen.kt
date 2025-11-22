package com.afilaxy.presentation.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val viewModel: ProfileViewModel = androidx.hilt.navigation.compose.hiltViewModel()
    
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "👤 Meu Perfil",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        // Email (somente leitura)
        OutlinedTextField(
            value = uiState.email,
            onValueChange = { },
            label = { Text("Email") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            enabled = false
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Nome/Apelido
        OutlinedTextField(
            value = uiState.name,
            onValueChange = { viewModel.updateName(it) },
            label = { Text("Nome/Apelido") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Como outros usuários te verão") }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Telefone
        OutlinedTextField(
            value = uiState.phone,
            onValueChange = { viewModel.updatePhone(it) },
            label = { Text("Telefone") },
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("(11) 99999-9999") }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Tipo de Asma
        Text(
            text = "Tipo de Asma",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        val asmaTypes = listOf(
            "Asma Leve Intermitente",
            "Asma Leve Persistente", 
            "Asma Moderada Persistente",
            "Asma Grave Persistente",
            "Asma Alérgica",
            "Asma não Alérgica",
            "Outro"
        )
        
        asmaTypes.forEach { type ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                RadioButton(
                    selected = uiState.asmaType == type,
                    onClick = { viewModel.updateAsmaType(type) }
                )
                Text(
                    text = type,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Medicamentos
        Text(
            text = "💊 Medicamentos que utilizo",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        val medicamentos = listOf(
            "Salbutamol (Aerolin)",
            "Budesonida",
            "Formoterol",
            "Beclometasona",
            "Prednisolona",
            "Montelucaste",
            "Teofilina",
            "Outro"
        )
        
        medicamentos.forEach { med ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = uiState.medications.contains(med),
                    onCheckedChange = { checked ->
                        if (checked) {
                            viewModel.addMedication(med)
                        } else {
                            viewModel.removeMedication(med)
                        }
                    }
                )
                Text(
                    text = med,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Botão Salvar
        Button(
            onClick = { viewModel.saveProfile() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text("💾 Salvar Perfil")
        }
        
        // Mensagem de sucesso/erro
        uiState.message?.let { message ->
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (uiState.isError) 
                        MaterialTheme.colorScheme.errorContainer 
                    else 
                        MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    text = message,
                    modifier = Modifier.padding(16.dp),
                    color = if (uiState.isError) 
                        MaterialTheme.colorScheme.onErrorContainer 
                    else 
                        MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}