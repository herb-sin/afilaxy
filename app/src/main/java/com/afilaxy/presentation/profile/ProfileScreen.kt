package com.afilaxy.presentation.profile

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModel
import com.afilaxy.presentation.home.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: HomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return HomeViewModel(context) as T
            }
        }
    )
    
    val sharedPrefs = context.getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
    
    var showNameDialog by remember { mutableStateOf(false) }
    var showPhoneDialog by remember { mutableStateOf(false) }
    var showAsthmaDialog by remember { mutableStateOf(false) }
    var showEmergencyDialog by remember { mutableStateOf(false) }
    var showMedicationsDialog by remember { mutableStateOf(false) }
    
    var userName by remember { mutableStateOf(sharedPrefs.getString("user_name", "Usuário Afilaxy") ?: "Usuário Afilaxy") }
    var userPhone by remember { mutableStateOf(sharedPrefs.getString("user_phone", "") ?: "") }
    var asthmaType by remember { mutableStateOf(sharedPrefs.getString("asthma_type", "") ?: "") }
    var emergencyContact by remember { mutableStateOf(sharedPrefs.getString("emergency_contact", "") ?: "") }
    var medications by remember { 
        val savedMeds = sharedPrefs.getStringSet("medications", emptySet()) ?: emptySet()
        mutableStateOf(savedMeds.toList())
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("👤 Perfil") },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() }
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Card do Usuário
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    // Nome/Apelido (apenas exibição)
                    Text(
                        text = userName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = viewModel.userEmail,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Membro desde Janeiro 2025",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Seção de Informações Pessoais
            Text(
                "Informações Pessoais",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    ProfileInfoItem(
                        icon = Icons.Default.Person,
                        label = "Nome/Apelido",
                        value = userName,
                        onClick = { showNameDialog = true }
                    )
                    
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    ProfileInfoItem(
                        icon = Icons.Default.Phone,
                        label = "Telefone",
                        value = if (userPhone.isEmpty()) "Adicionar telefone" else userPhone,
                        onClick = { showPhoneDialog = true }
                    )

                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Seção de Saúde
            Text(
                "Informações de Saúde",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    ProfileInfoItem(
                        icon = Icons.Default.Favorite,
                        label = "Tipo de Asma",
                        value = if (asthmaType.isEmpty()) "Não informado" else asthmaType,
                        onClick = { showAsthmaDialog = true }
                    )
                    
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    ProfileInfoItem(
                        icon = Icons.Default.Add,
                        label = "Medicamentos",
                        value = if (medications.isEmpty()) "Adicionar medicamentos" else "${medications.size} medicamento(s)",
                        onClick = { showMedicationsDialog = true }
                    )
                    
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    ProfileInfoItem(
                        icon = Icons.Default.Warning,
                        label = "Contato de Emergência",
                        value = if (emergencyContact.isEmpty()) "Adicionar contato" else emergencyContact,
                        onClick = { showEmergencyDialog = true }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            

        }
    }
    
    // Diálogo para editar nome
    if (showNameDialog) {
        var tempName by remember { mutableStateOf(userName) }
        AlertDialog(
            onDismissRequest = { showNameDialog = false },
            title = { Text("Editar Nome") },
            text = {
                OutlinedTextField(
                    value = tempName,
                    onValueChange = { tempName = it },
                    label = { Text("Nome ou Apelido") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        userName = tempName
                        sharedPrefs.edit().putString("user_name", tempName).apply()
                        showNameDialog = false
                    }
                ) {
                    Text("Salvar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showNameDialog = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
    
    // Diálogo para editar telefone
    if (showPhoneDialog) {
        var tempPhone by remember { mutableStateOf(userPhone) }
        AlertDialog(
            onDismissRequest = { showPhoneDialog = false },
            title = { Text("Editar Telefone") },
            text = {
                OutlinedTextField(
                    value = tempPhone,
                    onValueChange = { tempPhone = it },
                    label = { Text("Telefone") },
                    placeholder = { Text("(11) 99999-9999") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        userPhone = tempPhone
                        sharedPrefs.edit().putString("user_phone", tempPhone).apply()
                        showPhoneDialog = false
                    }
                ) {
                    Text("Salvar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showPhoneDialog = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
    
    // Diálogo para tipo de asma
    if (showAsthmaDialog) {
        val asthmaOptions = listOf(
            "Asma Alérgica (Atópica)",
            "Asma Não Alérgica (Intrínseca)",
            "Asma Ocupacional",
            "Broncoconstrição Induzida por Exercício (BIE)",
            "Asma Induzida por Medicamentos (AINEs)",
            "Asma Eosinofílica",
            "Asma Noturna",
            "Outro"
        )
        
        AlertDialog(
            onDismissRequest = { showAsthmaDialog = false },
            title = { Text("Selecione o Tipo de Asma") },
            text = {
                Column {
                    asthmaOptions.forEach { option ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = asthmaType == option,
                                onClick = {
                                    asthmaType = option
                                    sharedPrefs.edit().putString("asthma_type", option).apply()
                                    showAsthmaDialog = false
                                }
                            )
                            Text(
                                text = option,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(
                    onClick = { showAsthmaDialog = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
    
    // Diálogo para contato de emergência
    if (showEmergencyDialog) {
        var tempContact by remember { mutableStateOf(emergencyContact) }
        AlertDialog(
            onDismissRequest = { showEmergencyDialog = false },
            title = { Text("Contato de Emergência") },
            text = {
                OutlinedTextField(
                    value = tempContact,
                    onValueChange = { tempContact = it },
                    label = { Text("Nome e Telefone") },
                    placeholder = { Text("Ex: Maria - (11) 99999-9999") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        emergencyContact = tempContact
                        sharedPrefs.edit().putString("emergency_contact", tempContact).apply()
                        showEmergencyDialog = false
                    }
                ) {
                    Text("Salvar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showEmergencyDialog = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
    
    // Diálogo para medicamentos
    if (showMedicationsDialog) {
        var newMedication by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showMedicationsDialog = false },
            title = { Text("Meus Medicamentos") },
            text = {
                Column {
                    // Lista de medicamentos existentes
                    medications.forEach { medication ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = medication,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = {
                                    val updatedMeds = medications.filter { it != medication }
                                    medications = updatedMeds
                                    sharedPrefs.edit().putStringSet("medications", updatedMeds.toSet()).apply()
                                }
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Remover",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Campo para adicionar novo medicamento
                    OutlinedTextField(
                        value = newMedication,
                        onValueChange = { newMedication = it },
                        label = { Text("Novo medicamento") },
                        placeholder = { Text("Ex: Salbutamol, Budesonida") },
                        singleLine = true,
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    if (newMedication.isNotBlank()) {
                                        val updatedMeds = medications + newMedication.trim()
                                        medications = updatedMeds
                                        sharedPrefs.edit().putStringSet("medications", updatedMeds.toSet()).apply()
                                        newMedication = ""
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Adicionar")
                            }
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showMedicationsDialog = false }
                ) {
                    Text("Concluído")
                }
            },
            dismissButton = {}
        )
    }
    

}

@Composable
private fun ProfileInfoItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        IconButton(onClick = onClick) {
            Icon(
                Icons.Default.Edit,
                contentDescription = "Editar",
                modifier = Modifier.size(16.dp)
            )
        }
    }
}