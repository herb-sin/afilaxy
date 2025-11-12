package com.afilaxy

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.afilaxy.presentation.common.navigation.AppNavigation
import com.afilaxy.ui.theme.AfilaxyTheme
import com.afilaxy.performance.AnrOptimizer
import com.afilaxy.data.worker.WorkManagerInitializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            setContent {
                MainContent()
            }
            
            AnrOptimizer.executeAsync {
                initializeBackgroundServices()
            }
            
            WorkManagerInitializer.scheduleCleanupWork(this)
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error in onCreate: ${e.javaClass.simpleName}")
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
                    val shouldOpenEmergencyResponse = intent?.getBooleanExtra("open_emergency_response", false) ?: false
                    val emergencyId = intent?.getStringExtra("emergency_id")
                    
                    android.util.Log.d("MainActivity", "Checking emergency response: $shouldOpenEmergencyResponse, ID: $emergencyId")
                    
                    if (shouldOpenEmergencyResponse && !emergencyId.isNullOrEmpty()) {
                        android.util.Log.d("MainActivity", "Navigating to emergency response")
                        navController.navigate("emergency_response/$emergencyId/Pessoa")
                    }
                }
                
                AppNavigation(
                    navController = navController,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        AnrOptimizer.cleanup()
    }
}