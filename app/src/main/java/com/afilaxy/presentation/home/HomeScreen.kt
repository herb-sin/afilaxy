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
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
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
    val locationPermissions = rememberMultiplePermissionsState(
        permissions = listOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )
    
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
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
                        label = { Text("Logout") },
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
                    title = { Text("🫁 Afilaxy") },
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Bem-vindo!",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = viewModel.userEmail,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { 
                            viewModel.logout()
                            navController.navigate(AppRoutes.TELA_LOGIN) {
                                popUpTo(AppRoutes.TELA_INICIAL) { inclusive = true }
                            }
                        }
                    ) {
                        Text("Sair")
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Status Message
        if (viewModel.statusMessage.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    text = viewModel.statusMessage,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Helper Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Sou ajudante:")
                Text(
                    "Receber notificações de emergência",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = viewModel.isHelper,
                onCheckedChange = { newValue ->
                    if (newValue && !locationPermissions.allPermissionsGranted) {
                        locationPermissions.launchMultiplePermissionRequest()
                    } else {
                        viewModel.toggleHelper()
                    }
                }
            )
        }
        
        // Handle permission result - activate helper when permissions granted
        LaunchedEffect(locationPermissions.allPermissionsGranted) {
            if (locationPermissions.allPermissionsGranted && !viewModel.isHelper) {
                viewModel.toggleHelper()
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = { navController.navigate(AppRoutes.TELA_EMERGENCIA) },
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
}