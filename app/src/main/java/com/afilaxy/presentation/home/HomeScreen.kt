package com.afilaxy.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModel
import com.afilaxy.presentation.common.navigation.AppRoutes
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.isGranted
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.afilaxy.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: SimpleHomeViewModel = androidx.hilt.navigation.compose.hiltViewModel()
    val locationPermissions = rememberMultiplePermissionsState(
        permissions = listOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
        )
    )
    
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    // Refresh helper status when screen is displayed
    LaunchedEffect(Unit) {
        viewModel.onResume()
    }
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Menu",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        IconButton(
                            onClick = {
                                scope.launch { drawerState.close() }
                            }
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Fechar menu")
                        }
                    }
                    
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Person, contentDescription = null) },
                        label = { Text("Perfil") },
                        selected = false,
                        onClick = { 
                            navController.navigate(AppRoutes.TELA_PROFILE)
                            scope.launch { drawerState.close() }
                        }
                    )
                    
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Info, contentDescription = null) },
                        label = { Text("Sobre o Projeto") },
                        selected = false,
                        onClick = { 
                            navController.navigate(AppRoutes.TELA_SOBRE_PROJETO)
                            scope.launch { drawerState.close() }
                        }
                    )
                    
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Info, contentDescription = null) },
                        label = { Text("Termos de Uso") },
                        selected = false,
                        onClick = { 
                            navController.navigate(AppRoutes.TELA_TERMOS)
                            scope.launch { drawerState.close() }
                        }
                    )
                    
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        label = { Text("Política de Privacidade") },
                        selected = false,
                        onClick = { 
                            navController.navigate(AppRoutes.TELA_LGPD)
                            scope.launch { drawerState.close() }
                        }
                    )
                    
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.ExitToApp, contentDescription = null) },
                        label = { Text("Sair") },
                        selected = false,
                        onClick = {
                            viewModel.logout()
                            navController.navigate(AppRoutes.TELA_LOGIN) {
                                popUpTo(AppRoutes.TELA_INICIAL) { inclusive = true }
                            }
                        }
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.afilaxy_icon_48),
                                contentDescription = "Logo Afilaxy",
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Afilaxy")
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    drawerState.open()
                                }
                            }
                        ) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
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
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
        Text(
            "Comunidade Afilaxy",
            style = MaterialTheme.typography.headlineMedium
        )
        
        if (viewModel.isLoggedIn) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(if (viewModel.isHelper) "Estou com a \"bombinha\"" else "Não estou com a \"bombinha\"")
                        Text(
                            if (viewModel.isHelper) "Receber pedidos de emergência" else "Não receber pedidos de emergência",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = viewModel.isHelper,
                        onCheckedChange = { _ ->
                            viewModel.toggleHelper()
                        }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        

        

        

        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = { 
                // Verificar se há emergência ativa antes de navegar
                scope.launch {
                    val activeEmergencyId = viewModel.checkForActiveEmergency()
                    if (activeEmergencyId != null) {
                        // Retomar emergência ativa
                        navController.navigate("emergency_response/$activeEmergencyId/Retomando")
                    } else {
                        // Nova emergência
                        navController.navigate(AppRoutes.TELA_EMERGENCIA)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text("🆘 EMERGÊNCIA")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = { navController.navigate(AppRoutes.TELA_AUTOCUIDADO) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("💚 Autocuidado")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = { navController.navigate(AppRoutes.TELA_COMUNIDADE) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Comunidade")
        }
            }
        }
    }
    
    // Diálogo customizado para permissão de localização
    if (viewModel.showLocationDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissLocationDialog() },
            title = { Text("Permissão de Localização") },
            text = {
                Text(
                    "Para receber pedidos de emergência, é necessário permitir acesso à localização \"o tempo todo\".\n\n" +
                    "Na próxima tela, selecione \"Permitir o tempo todo\" para ativar como helper.",
                    textAlign = TextAlign.Start
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.dismissLocationDialog()
                        locationPermissions.launchMultiplePermissionRequest()
                    }
                ) {
                    Text("Entendi")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.dismissLocationDialog() }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
    
    // Diálogo de permissões necessárias
    if (viewModel.showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissPermissionDialog() },
            title = { Text("Permissões Necessárias") },
            text = {
                Text(
                    viewModel.permissionMessage + "\n\n" +
                    "Vá em Configurações > Apps > Afilaxy > Permissões e ative:",
                    textAlign = TextAlign.Start
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.dismissPermissionDialog()
                        // Tentar solicitar permissões novamente
                        locationPermissions.launchMultiplePermissionRequest()
                    }
                ) {
                    Text("Tentar Novamente")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.dismissPermissionDialog() }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}