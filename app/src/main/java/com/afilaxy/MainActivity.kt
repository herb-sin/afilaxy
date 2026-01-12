package com.afilaxy

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.afilaxy.presentation.common.navigation.AppNavigation
import com.afilaxy.ui.theme.AfilaxyTheme
import com.afilaxy.performance.AnrOptimizer
// import com.afilaxy.data.worker.WorkManagerInitializer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.afilaxy.security.SecureLogger

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private var emergencyNavigationState = mutableStateOf<String?>(null)
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge for Android 15 compatibility
        enableEdgeToEdge()
        
        try {
            SecureLogger.d("MainActivity", "onCreate - Intent extras: ${intent?.extras?.keySet()}")
            
            setContent {
                MainContent()
            }
            
            AnrOptimizer.executeAsync {
                initializeBackgroundServices()
            }
            
            // WorkManagerInitializer.scheduleCleanupWork(this)
        } catch (e: Exception) {
            SecureLogger.e("MainActivity", "Error in onCreate: ${e.javaClass.simpleName}")
            finish()
        }
    }
    

    
    private suspend fun initializeBackgroundServices() {
        withContext(Dispatchers.IO) {
            // Any heavy initialization here
        }
    }
    
    @Composable
    private fun MainContent() {
        AfilaxyTheme {
            Surface(modifier = Modifier.fillMaxSize()) {
                val navController = rememberNavController()
                
                // Verificar se deve abrir resposta à emergência
                LaunchedEffect(navController) {
                    SecureLogger.d("MainActivity", "LaunchedEffect iniciado")
                    // Aguardar NavController estar pronto
                    kotlinx.coroutines.delay(100)
                    checkEmergencyIntent(navController)
                }
                
                // Reagir a mudanças no estado de navegação
                LaunchedEffect(emergencyNavigationState.value) {
                    val route = emergencyNavigationState.value
                    SecureLogger.d("MainActivity", "LaunchedEffect - Route: $route")
                    
                    if (!route.isNullOrEmpty()) {
                        SecureLogger.d("MainActivity", "LaunchedEffect - Navegando para: $route")
                        try {
                            navController.navigate(route)
                            SecureLogger.d("MainActivity", "LaunchedEffect - Navegação executada com sucesso")
                            emergencyNavigationState.value = null // Reset
                        } catch (e: Exception) {
                            SecureLogger.e("MainActivity", "LaunchedEffect - Erro na navegação: ${e.message}")
                        }
                    }
                }
                
                AppNavigation(
                    navController = navController,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
    

    
    private fun checkEmergencyIntent(navController: androidx.navigation.NavHostController) {
        SecureLogger.d("MainActivity", "Intent extras: ${intent?.extras?.keySet()}")
        val shouldOpenEmergencyResponse = intent?.getBooleanExtra("open_emergency_response", false) ?: false
        val emergencyId = intent?.getStringExtra("emergency_id")
        
        SecureLogger.d("MainActivity", "Checking emergency response: $shouldOpenEmergencyResponse, ID: $emergencyId")
        
        if (shouldOpenEmergencyResponse && !emergencyId.isNullOrEmpty()) {
            SecureLogger.d("MainActivity", "Navigating to emergency response with ID: $emergencyId")
            navController.navigate("emergency_response/$emergencyId/Helper")
        } else {
            SecureLogger.d("MainActivity", "No emergency navigation needed, staying on home")
        }
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        SecureLogger.d("MainActivity", "onNewIntent - Intent extras: ${intent.extras?.keySet()}")
        setIntent(intent)
        
        // Verificar se é uma emergência (tanto response quanto request)
        val shouldOpenEmergencyResponse = intent.getBooleanExtra("open_emergency_response", false)
        val emergencyId = intent.getStringExtra("emergency_id")
        
        SecureLogger.d("MainActivity", "onNewIntent - Emergency check: $shouldOpenEmergencyResponse, ID: $emergencyId")
        
        if (shouldOpenEmergencyResponse && !emergencyId.isNullOrEmpty()) {
            val requesterName = intent.getStringExtra("requester_name") ?: "Helper"
            SecureLogger.d("MainActivity", "onNewIntent - Triggering navigation to: emergency_response/$emergencyId/$requesterName")
            emergencyNavigationState.value = "emergency_response/$emergencyId/$requesterName"
        } else if (!emergencyId.isNullOrEmpty()) {
            // Se tem emergencyId mas não é response, pode ser uma notificação normal
            SecureLogger.d("MainActivity", "onNewIntent - Emergency ID found but not response, staying on home")
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        AnrOptimizer.cleanup()
    }
}