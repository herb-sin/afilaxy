package com.afilaxy

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
        
        // Set content immediately to avoid ANR
        setContent {
            MainContent()
        }
        
        // Initialize heavy operations in background
        AnrOptimizer.executeAsync {
            initializeBackgroundServices()
        }
        
        // Schedule cleanup work
        WorkManagerInitializer.scheduleCleanupWork(this)
    }
    
    private suspend fun initializeBackgroundServices() {
        withContext(Dispatchers.IO) {
            // Any heavy initialization here
        }
    }
    
    @Composable
    private fun MainContent() {
        AfilaxyTheme {
            val navController = rememberNavController()
            
            Surface(modifier = Modifier.fillMaxSize()) {
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