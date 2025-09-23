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
import com.afilaxy.utils.FirebaseDiagnostic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.google.android.gms.location.LocationCallback
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration


class MainActivity : ComponentActivity() {
    private var locationCallback: LocationCallback? = null
    private var notificationListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Inicializar Firebase com configurações específicas
        try {
            FirebaseApp.initializeApp(this)
            
            // Configurar Firebase para emulador se necessário
            val auth = FirebaseAuth.getInstance()
            val firestore = FirebaseFirestore.getInstance()
            
            // Detectar se está rodando no emulador
            val isEmulator = android.os.Build.FINGERPRINT.contains("generic") ||
                           android.os.Build.MODEL.contains("Emulator") ||
                           android.os.Build.MANUFACTURER.contains("Genymotion")
            
            if (isEmulator) {
                android.util.Log.d("MainActivity", "🔧 Configurando Firebase para emulador")
                // Configurações específicas para emulador
                try {
                    auth.useEmulator("10.0.2.2", 9099)
                    firestore.useEmulator("10.0.2.2", 8080)
                    android.util.Log.d("MainActivity", "✅ Emulador Firebase configurado")
                } catch (e: Exception) {
                    android.util.Log.w("MainActivity", "⚠️ Erro ao configurar emulador: ${e.message}")
                }
            }
            
            android.util.Log.d("MainActivity", "✅ Firebase inicializado com sucesso")
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "❌ Erro ao inicializar Firebase: ${e.message}")
        }
        
        // Executar diagnóstico Firebase
        CoroutineScope(Dispatchers.IO).launch {
            FirebaseDiagnostic.runFullDiagnostic(this@MainActivity)
        }

        // Solicita permissão de localização
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

        setContent {
            RequestNotificationPermission()
            AfilaxyTheme {
                val navController = rememberNavController()
                
                // Listener para notificações de emergência em tempo real
                LaunchedEffect(Unit) {
                    val auth = FirebaseAuth.getInstance()
                    val firestore = FirebaseFirestore.getInstance()
                    val currentUser = auth.currentUser
                    
                    if (currentUser != null) {
                        android.util.Log.d("MainActivity", "🔍 ✅ Configurando listener para: ${currentUser.uid} (${currentUser.email})")
                        
                        notificationListener = firestore
                            .collection("users")
                            .document(currentUser.uid)
                            .collection("notifications")
                            .whereEqualTo("type", "emergency_alert")
                            .addSnapshotListener { snapshot, error ->
                                if (error != null) {
                                    android.util.Log.e("MainActivity", "❌ Erro no listener: ${error.message}")
                                    return@addSnapshotListener
                                }
                                
                                android.util.Log.d("MainActivity", "📨 ✅ Listener ATIVO - Documentos: ${snapshot?.documents?.size ?: 0}")
                                
                                snapshot?.documentChanges?.forEach { change ->
                                    android.util.Log.d("MainActivity", "🔄 🔥 MUDANÇA: ${change.type}")
                                    
                                    if (change.type == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                                        android.util.Log.d("MainActivity", "🎆 🚨 EMERGÊNCIA DETECTADA!")
                                        
                                        val emergencyId = change.document.getString("emergencyId")
                                        val requesterName = change.document.getString("requesterName")
                                        val requesterId = change.document.getString("requesterId")
                                        
                                        android.util.Log.d("MainActivity", "🎯 ID: $emergencyId, De: $requesterName, RequesterId: $requesterId")
                                        android.util.Log.d("MainActivity", "👤 Current User: ${currentUser.uid}")
                                        
                                        // Verificar se não é o próprio usuário
                                        if (requesterId != currentUser.uid) {
                                            android.util.Log.d("MainActivity", "✅ Emergência de outro usuário - navegando")
                                            if (emergencyId != null) {
                                                navController.navigate("tela_helper_response/$emergencyId")
                                            } else {
                                                navController.navigate("tela_helper_response")
                                            }
                                        } else {
                                            android.util.Log.d("MainActivity", "🚫 Emergência própria - ignorando")
                                        }
                                    }
                                }
                            }
                    } else {
                        android.util.Log.e("MainActivity", "❌ Usuário NÃO autenticado")
                    }
                }
                
                // Verificar se app foi aberto por notificação de emergência
                LaunchedEffect(Unit) {
                    if (intent.getBooleanExtra("open_emergency", false)) {
                        android.util.Log.d("MainActivity", "📨 App aberto por notificação de emergência")
                        navController.navigate("tela_helper_response")
                    }
                }
                val context = LocalContext.current
                var isLocationUpdatesActive by remember { mutableStateOf(false) }

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    val requestPermissionLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestPermission(),
                        onResult = { /* handle notification permission result */ }
                    )
                    LaunchedEffect(Unit) {
                        requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                    }
                }

                // Launcher para solicitar permissão de localização
                val requestLocationPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                    onResult = { isGranted: Boolean ->
                        if (isGranted) {
                            // Inicia atualização contínua de localização
                            if (!isLocationUpdatesActive) {
                                locationCallback = startSignificantMovementUpdates(
                                    context,
                                    minDistanceMeters = 50f
                                ) { lat, lon ->
                                    saveUserLocationWithCoords(context, lat, lon)
                                }
                                isLocationUpdatesActive = true
                            }
                        }
                    }
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
                            stopLocationUpdates(context, it)
                        }
                        notificationListener?.remove()
                        isLocationUpdatesActive = false
                    }
                }
            }
        }
    }
}