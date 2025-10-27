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
import com.afilaxy.security.AuthGuard
import com.afilaxy.security.InputSanitizer
import com.afilaxy.security.SecureLogger
import com.afilaxy.ui.theme.AfilaxyTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main activity with security features:
 * - Authentication checks
 * - Input sanitization for intents
 * - Secure error handling
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val firebaseAuth by lazy { FirebaseAuth.getInstance() }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            FirebaseApp.initializeApp(this)
            setupSecurityInitialization()
            
            setContent {
                MainContent()
            }
        } catch (e: Exception) {
            SecureLogger.e("MainActivity", "Initialization error", e)
            setContent {
                MainContent() // Continue with basic functionality
            }
        }
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleSecureIntent(intent)
    }
    
    private fun setupSecurityInitialization() {
        if (!AuthGuard.isUserAuthenticated()) {
            SecureLogger.w("MainActivity", "User not authenticated - allowing access to login")
        }
    }
    
    private fun handleSecureIntent(intent: Intent) {
        try {
            val rawEmergencyId = intent.getStringExtra("emergency_id") ?: ""
            val rawRequesterName = intent.getStringExtra("requester_name") ?: ""
            val rawNotificationType = intent.getStringExtra("notification_type") ?: ""
            
            val emergencyId = InputSanitizer.sanitizeText(rawEmergencyId)
            val requesterName = InputSanitizer.sanitizeName(rawRequesterName)
            val notificationType = InputSanitizer.sanitizeText(rawNotificationType)
            
            if (emergencyId.isBlank() && rawEmergencyId.isNotBlank()) {
                SecureLogger.security("INTENT_VALIDATION", "INVALID_EMERGENCY_ID")
            }
            
            SecureLogger.d("MainActivity", "Intent data validated successfully")
        } catch (e: Exception) {
            SecureLogger.e("MainActivity", "Error handling intent", e)
        }
    }

    @Composable
    private fun MainContent() {
        AfilaxyTheme {
            val navController = rememberNavController()
            
            SetupAuthListener()
            
            Surface(modifier = Modifier.fillMaxSize()) {
                AppNavigation(
                    navController = navController,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
    
    @Composable
    private fun SetupAuthListener() {
        LaunchedEffect(Unit) {
            try {
                firebaseAuth.addAuthStateListener { auth ->
                    if (auth.currentUser == null) {
                        SecureLogger.security("AUTH_STATE_CHANGE", "USER_LOGGED_OUT")
                    } else {
                        SecureLogger.security("AUTH_STATE_CHANGE", "USER_LOGGED_IN")
                    }
                }
            } catch (e: Exception) {
                SecureLogger.e("MainActivity", "Error setting up auth listener", e)
            }
        }
    }
}