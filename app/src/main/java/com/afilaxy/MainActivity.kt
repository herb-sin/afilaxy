package com.afilaxy

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.navigation.compose.rememberNavController
import com.afilaxy.presentation.common.navigation.AppNavigation
import com.afilaxy.ui.theme.AfilaxyTheme
import com.afilaxy.ui.RequestNotificationPermission

import com.afilaxy.notification.NotificationManager
import com.afilaxy.security.AuthGuard
import com.google.android.gms.location.LocationCallback
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth


class MainActivity : ComponentActivity() {
    private var locationCallback: LocationCallback? = null
    private val notificationManager = NotificationManager()
    
    // Instâncias Firebase otimizadas com lazy initialization
    private val firebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        initializeFirebase()
        setupLocationPermissions()
        
        setContent {
            RequestNotificationPermission()
            AfilaxyTheme {
                val navController = rememberNavController()
                
                // Estado para observar usuário logado
                var currentUser by remember { mutableStateOf(firebaseAuth.currentUser) }
                
                // Observar mudanças de autenticação
                LaunchedEffect(Unit) {
                    firebaseAuth.addAuthStateListener { auth ->
                        currentUser = auth.currentUser
                    }
                }
                
                // Configurar listener de notificações
                LaunchedEffect(currentUser) {
                    if (currentUser != null) {
                        notificationManager.setupNotificationListener(navController)
                    } else {
                        notificationManager.cleanup()
                    }
                }
                
                // Verificar se app foi aberto por notificação de emergência
                LaunchedEffect(Unit) {
                    if (intent.getBooleanExtra("open_emergency", false)) {
                        navController.navigate("tela_helper_response")
                    }
                }
                val context = LocalContext.current
                var isLocationUpdatesActive by remember { mutableStateOf(false) }
                
                // Gerenciamento de permissões de notificação
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val requestPermissionLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestPermission()
                    ) { /* handle notification permission result */ }
                    
                    LaunchedEffect(Unit) {
                        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
                
                // Gerenciamento de localização simplificado
                val locationManager = remember { com.afilaxy.location.LocationManager() }
                locationManager.SetupLocationPermissions(
                    onLocationCallback = { callback -> locationCallback = callback },
                    onLocationUpdatesActive = { active -> isLocationUpdatesActive = active }
                )

                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    AppNavigation(
                        navController = navController,
                        modifier = Modifier.fillMaxSize(),
                        onLocationCallbackUpdate = { callback ->
                            locationCallback = callback as? LocationCallback
                        }
                    )
                }

                // Cleanup location updates e listener
                DisposableEffect(isLocationUpdatesActive) {
                    onDispose {
                        locationCallback?.let {
                            try {
                                stopLocationUpdates(context, it)
                            } catch (e: Exception) {
                                android.util.Log.e("MainActivity", "Erro ao parar atualizações de localização: ${e.message}")
                            }
                        }
                        notificationManager.cleanup()
                        isLocationUpdatesActive = false
                    }
                }
            }
        }
    }
    
    private fun initializeFirebase() {
        try {
            FirebaseApp.initializeApp(this)
            
            val auth = firebaseAuth
            
            val isEmulator = android.os.Build.FINGERPRINT.contains("generic") ||
                           android.os.Build.MODEL.contains("Emulator") ||
                           android.os.Build.MANUFACTURER.contains("Genymotion")
            
            if (isEmulator) {
                try {
                    auth.useEmulator("10.0.2.2", 9099)
                } catch (e: Exception) {
                    android.util.Log.w("MainActivity", "Erro ao configurar emulador: ${e.message}")
                }
            }

        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "❌ Erro ao inicializar Firebase: ${e.message}")
        }
    }
    
    private fun setupLocationPermissions() {
        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                    1001
                )
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
}