package com.afilaxy.security

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController

object AuthInterceptor {
    
    private val protectedRoutes = setOf(
        "tela_emergency",
        "tela_helper_response",
        "tela_profile",
        "tela_settings",
        "tela_autocuidado"
    )
    
    @Composable
    fun RequireAuth(
        route: String,
        navController: NavController,
        content: @Composable () -> Unit
    ) {
        LaunchedEffect(route) {
            if (protectedRoutes.contains(route) && !AuthGuard.isAuthenticated()) {
                navController.navigate("tela_login") {
                    popUpTo(0) { inclusive = true }
                }
                return@LaunchedEffect
            }
        }
        
        if (!protectedRoutes.contains(route) || AuthGuard.isAuthenticated()) {
            content()
        }
    }
    
    fun requireAuthForOperation(operation: String): Boolean {
        if (!AuthGuard.isAuthenticated()) {
            android.util.Log.w("AuthInterceptor", "Operation '$operation' requires authentication")
            return false
        }
        return true
    }
    
    fun requireVerifiedEmailForOperation(operation: String): Boolean {
        if (!AuthGuard.isEmailVerified()) {
            android.util.Log.w("AuthInterceptor", "Operation '$operation' requires verified email")
            return false
        }
        return true
    }
}