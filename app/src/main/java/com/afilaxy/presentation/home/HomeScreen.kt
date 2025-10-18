package com.afilaxy.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.afilaxy.presentation.common.navigation.AppRoutes
import com.afilaxy.ui.theme.AfilaxyTheme
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import com.afilaxy.utils.LocationHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    LaunchedEffect(Unit) {
        viewModel.loadUserData()
        
        // Obter e salvar localização do usuário
        try {
            val location = LocationHelper.getCurrentLocation(context)
            location?.let {
                viewModel.updateUserLocation(it.latitude, it.longitude)
            }
        } catch (e: Exception) {
            android.util.Log.e("HomeScreen", "Erro ao obter localização: ${e.message}")
        }
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
                            navController.navigate("perfil")
                        }
                    )
                    
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Info, contentDescription = null) },
                        label = { Text("Termos de Uso") },
                        selected = false,
                        onClick = { 
                            navController.navigate(AppRoutes.TELA_TERMOS)
                        }
                    )
                    
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        label = { Text("Política de Privacidade") },
                        selected = false,
                        onClick = { 
                            navController.navigate(AppRoutes.TELA_LGPD)
                        }
                    )
                    
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.ExitToApp, contentDescription = null) },
                        label = { Text("Logout") },
                        selected = false,
                        onClick = {
                            try {
                                FirebaseAuth.getInstance().signOut()
                                navController.navigate(AppRoutes.TELA_LOGIN) {
                                    popUpTo(0) { inclusive = true }
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("HomeScreen", "Erro ao fazer logout: ${e.message}")
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
                    title = { Text("Afilaxy") },
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
                    .padding(paddingValues)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
        Text(
            text = "Bem-vindo(a) ao Afilaxy!",
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Sua comunidade de apoio e autocuidado para Asma.",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        
        // Card com switch de helper
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Disponível para ajudar",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = if (uiState.isHelper) "Você receberá pedidos de emergência" 
                               else "Você não receberá pedidos de emergência",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = uiState.isHelper,
                    onCheckedChange = { viewModel.toggleHelperStatus() }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                navController.navigate(AppRoutes.TELA_EMERGENCIA)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Emergência: Localizar Bombinha")
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(
            onClick = {
                navController.navigate(AppRoutes.TELA_COMUNIDADE)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Acessar Comunidade")
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(
            onClick = {
                navController.navigate(AppRoutes.TELA_AUTOCUIDADO)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Informações e Autocuidado")
        }

        
                uiState.errorMessage?.let { error ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    AfilaxyTheme {
        HomeScreen(navController = rememberNavController())
    }
}