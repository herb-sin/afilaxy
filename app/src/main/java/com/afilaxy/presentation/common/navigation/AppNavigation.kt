package com.afilaxy.presentation.common.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

    NavHost(
        navController = navController, 
        startDestination = AppRoutes.TELA_LOGIN
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
    }
}