package com.afilaxy.presentation.common.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.afilaxy.LocationPermissionDialog
import com.afilaxy.presentation.autocuidado.AutocuidadoScreen
import com.afilaxy.presentation.comunidade.ComunidadeScreen
import com.afilaxy.presentation.emergency.EmergencyScreen
import com.afilaxy.presentation.helper.HelperResponseScreen
import com.afilaxy.presentation.home.HomeScreen
import com.afilaxy.presentation.login.LoginScreen
import com.afilaxy.saveFcmTokenToFirestore

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    onLocationCallbackUpdate: (Any?) -> Unit = {}
) {
    var showLocationDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var isCheckingAuth by remember { mutableStateOf(true) }
    var startDestination by remember { mutableStateOf(AppRoutes.TELA_LOGIN) }
    
    // Verificar se usuário já está logado
    LaunchedEffect(Unit) {
        try {
            val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
            startDestination = if (currentUser != null && currentUser.isEmailVerified) {
                AppRoutes.TELA_INICIAL
            } else {
                AppRoutes.TELA_LOGIN
            }
        } catch (e: Exception) {
            android.util.Log.e("AppNavigation", "Erro ao verificar autenticação: ${e.message}")
            startDestination = AppRoutes.TELA_LOGIN
        }
        isCheckingAuth = false
    }
    
    if (isCheckingAuth) {
        // Tela de loading enquanto verifica autenticação
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    NavHost(
        navController = navController, 
        startDestination = startDestination
    ) {
        composable(AppRoutes.TELA_LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    showLocationDialog = true
                    saveFcmTokenToFirestore(context)
                }
            )
            if (showLocationDialog) {
                LocationPermissionDialog(
                    onConfirm = {
                        showLocationDialog = false
                        navController.navigate(AppRoutes.TELA_INICIAL) {
                            popUpTo(AppRoutes.TELA_LOGIN) { inclusive = true }
                        }
                    },
                    onDismiss = {
                        showLocationDialog = false
                    }
                )
            }
        }
        
        composable(AppRoutes.TELA_INICIAL) {
            HomeScreen(
                navController = navController,
                modifier = modifier
            )
        }
        
        composable(AppRoutes.TELA_EMERGENCIA) {
            EmergencyScreen(
                navController = navController,
                modifier = modifier
            )
        }
        
        composable(AppRoutes.TELA_AUTOCUIDADO) {
            AutocuidadoScreen(
                navController = navController,
                modifier = modifier
            )
        }
        
        composable(AppRoutes.TELA_COMUNIDADE) {
            ComunidadeScreen(
                navController = navController,
                modifier = modifier
            )
        }
        
        composable(
            route = "${AppRoutes.TELA_HELPER_RESPONSE}/{emergencyId}",
            arguments = listOf(navArgument("emergencyId") { type = NavType.StringType })
        ) { backStackEntry ->
            val emergencyId = backStackEntry.arguments?.getString("emergencyId")
            HelperResponseScreen(
                navController = navController,
                emergencyId = emergencyId,
                modifier = modifier
            )
        }
        
        // Rota sem parâmetro para compatibilidade
        composable(AppRoutes.TELA_HELPER_RESPONSE) {
            HelperResponseScreen(
                navController = navController,
                modifier = modifier
            )
        }
        
        composable("perfil") {
            com.afilaxy.presentation.profile.ProfileScreen(
                navController = navController,
                modifier = modifier
            )
        }
    }
}