package com.afilaxy.presentation.common.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.auth.FirebaseAuth
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
// import com.afilaxy.presentation.emergency.EmergencyScreenMaps
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
    
    // Check authentication state
    LaunchedEffect(Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        startDestination = when {
            currentUser == null -> AppRoutes.TELA_LOGIN
            !currentUser.isEmailVerified -> "email_verification"
            else -> AppRoutes.TELA_INICIAL
        }
        isCheckingAuth = false
    }
    
    if (isCheckingAuth) {
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
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    if (currentUser?.isEmailVerified == true) {
                        showLocationDialog = true
                        saveFcmTokenToFirestore(context)
                    } else {
                        navController.navigate("email_verification") {
                            popUpTo(AppRoutes.TELA_LOGIN) { inclusive = true }
                        }
                    }
                }
            )
            if (showLocationDialog) {
                LocationPermissionDialog(
                    onConfirm = {
                        showLocationDialog = false
                        navController.navigate(AppRoutes.TELA_INICIAL) {
                            popUpTo(AppRoutes.TELA_LOGIN) { 
                                inclusive = true 
                            }
                        }
                    },
                    onDismiss = {
                        showLocationDialog = false
                    }
                )
            }
        }
        
        composable("email_verification") {
            com.afilaxy.presentation.auth.EmailVerificationScreen(
                onVerified = {
                    showLocationDialog = true
                    saveFcmTokenToFirestore(context)
                },
                onLogout = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate(AppRoutes.TELA_LOGIN) {
                        popUpTo("email_verification") { inclusive = true }
                    }
                }
            )
            if (showLocationDialog) {
                LocationPermissionDialog(
                    onConfirm = {
                        showLocationDialog = false
                        navController.navigate(AppRoutes.TELA_INICIAL) {
                            popUpTo("email_verification") { 
                                inclusive = true 
                            }
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
            com.afilaxy.presentation.emergency.EmergencyBaseScreen(
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
            route = "emergency_response/{emergencyId}/{requesterName}",
            arguments = listOf(
                navArgument("emergencyId") { type = NavType.StringType },
                navArgument("requesterName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val emergencyId = backStackEntry.arguments?.getString("emergencyId") ?: ""
            
            com.afilaxy.presentation.emergency.EmergencyBaseScreen(
                navController = navController,
                emergencyId = emergencyId,
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
        
        composable(AppRoutes.TELA_PROFILE) {
            com.afilaxy.presentation.profile.ProfileScreen(
                navController = navController,
                modifier = modifier
            )
        }
        
        composable(
            route = "navigation/{latitude}/{longitude}/{title}",
            arguments = listOf(
                navArgument("latitude") { type = NavType.StringType },
                navArgument("longitude") { type = NavType.StringType },
                navArgument("title") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val latitude = backStackEntry.arguments?.getString("latitude")?.toDoubleOrNull() ?: 0.0
            val longitude = backStackEntry.arguments?.getString("longitude")?.toDoubleOrNull() ?: 0.0
            val title = backStackEntry.arguments?.getString("title") ?: "Destino"
            
            com.afilaxy.presentation.map.MapScreen(
                navController = navController
            )
        }
        
        composable(AppRoutes.TELA_TERMOS) {
            com.afilaxy.ui.screens.TermsScreen(navController)
        }
        
        composable(AppRoutes.TELA_SOBRE_PROJETO) {
            com.afilaxy.presentation.sobre.SobreProjetoScreen(
                navController = navController,
                modifier = modifier
            )
        }
        
        composable(AppRoutes.TELA_LGPD) {
            com.afilaxy.ui.screens.LGPDScreen(navController)
        }
        
        composable(AppRoutes.TELA_MAPA) {
            com.afilaxy.presentation.map.MapScreen(
                navController = navController,
                modifier = modifier
            )
        }
        
        composable(
            route = "navigation/{lat}/{lng}/{name}",
            arguments = listOf(
                navArgument("lat") { type = NavType.StringType },
                navArgument("lng") { type = NavType.StringType },
                navArgument("name") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val lat = backStackEntry.arguments?.getString("lat")?.toDoubleOrNull() ?: 0.0
            val lng = backStackEntry.arguments?.getString("lng")?.toDoubleOrNull() ?: 0.0
            val name = backStackEntry.arguments?.getString("name") ?: "Pessoa em emergência"
            
            com.afilaxy.presentation.navigation.NavigationScreen(
                destinationLat = lat,
                destinationLng = lng,
                destinationName = name,
                onBackPressed = { navController.popBackStack() }
            )
        }
        

    }
}