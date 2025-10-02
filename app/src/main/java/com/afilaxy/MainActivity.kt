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
import com.afilaxy.utils.ErrorHandler
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
    
    // Instâncias Firebase otimizadas com lazy initialization
    private val firebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firebaseFirestore by lazy { FirebaseFirestore.getInstance() }

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
                        android.util.Log.d("MainActivity", "🔄 Auth state changed: ${currentUser?.uid ?: "NULL"}")
                    }
                }
                
                // Listener para notificações de emergência em tempo real
                LaunchedEffect(currentUser) {
                    android.util.Log.d("MainActivity", "🚀 INICIANDO CONFIGURAÇÃO DO LISTENER")
                    val auth = firebaseAuth
                    val firestore = firebaseFirestore
                    android.util.Log.d("MainActivity", "👤 Current user no LaunchedEffect: ${currentUser?.uid ?: "NULL"}")
                    
                    // Verificação crítica de autenticação
                    if (currentUser == null) {
                        android.util.Log.w("MainActivity", "❌ Usuário não autenticado - listener de notificações não configurado")
                        notificationListener?.remove()
                        return@LaunchedEffect
                    }
                    
                    if (!currentUser!!.isEmailVerified) {
                        android.util.Log.w("MainActivity", "Email não verificado - listener de notificações limitado")
                    }
                    
                    android.util.Log.d("MainActivity", "🔍 ✅ Configurando listener para: ${currentUser!!.uid} (${currentUser!!.email})")
                    
                    android.util.Log.d("MainActivity", "🎯 Configurando listener para path: users/${currentUser!!.uid}/notifications")
                    
                    notificationListener = firestore
                        .collection("users")
                        .document(currentUser!!.uid)
                        .collection("notifications")
                        .addSnapshotListener { snapshot, error ->
                            if (error != null) {
                                val errorResult = ErrorHandler.handleError(error, "notificationListener")
                                android.util.Log.e("MainActivity", errorResult.logMessage)
                                return@addSnapshotListener
                            }
                            
                            android.util.Log.d("MainActivity", "📨 ✅ LISTENER ATIVO - Total docs: ${snapshot?.documents?.size ?: 0}")
                            
                            // Log todos os documentos para debug
                            snapshot?.documents?.forEach { doc ->
                                val type = doc.getString("type")
                                val processed = doc.getBoolean("processed") ?: false
                                android.util.Log.d("MainActivity", "📄 Doc: ${doc.id}, Type: $type, Processed: $processed")
                            }
                            
                            snapshot?.documentChanges?.forEach { change ->
                                android.util.Log.d("MainActivity", "🔄 🔥 MUDANÇA DETECTADA: ${change.type}")
                                
                                if (change.type == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                                    val doc = change.document
                                    val type = doc.getString("type")
                                    val processed = doc.getBoolean("processed") ?: false
                                    
                                    android.util.Log.d("MainActivity", "📋 Novo doc: ID=${doc.id}, Type=$type, Processed=$processed")
                                    
                                    if (type == "emergency_alert" && !processed) {
                                        android.util.Log.d("MainActivity", "🎆 🚨 EMERGÊNCIA DETECTADA!")
                                        
                                        val emergencyId = doc.getString("emergencyId")
                                        val requesterName = doc.getString("requesterName")
                                        val requesterId = doc.getString("requesterId")
                                        
                                        android.util.Log.d("MainActivity", "🎯 EmergencyId: $emergencyId")
                                        android.util.Log.d("MainActivity", "🎯 RequesterName: $requesterName")
                                        android.util.Log.d("MainActivity", "🎯 RequesterId: $requesterId")
                                        android.util.Log.d("MainActivity", "👤 CurrentUser: ${currentUser!!.uid}")
                                        
                                        // Marcar como processado com tratamento de erro
                                        ErrorHandler.safeCall(
                                            operation = "markNotificationProcessed",
                                            onError = { error ->
                                                android.util.Log.e("MainActivity", "Falha ao marcar notificação: ${error.logMessage}")
                                            }
                                        ) {
                                            doc.reference.update("processed", true)
                                                .addOnSuccessListener {
                                                    android.util.Log.d("MainActivity", "✅ Notificação marcada como processada")
                                                }
                                        }
                                        
                                        // Verificar se não é o próprio usuário
                                        if (requesterId != null && requesterId != currentUser!!.uid) {
                                            android.util.Log.d("MainActivity", "✅ Emergência de outro usuário - NAVEGANDO AGORA!")
                                            if (emergencyId != null) {
                                                navController.navigate("tela_helper_response/$emergencyId")
                                            } else {
                                                navController.navigate("tela_helper_response")
                                            }
                                        } else {
                                            android.util.Log.d("MainActivity", "🚫 Emergência própria ou requesterId nulo - ignorando")
                                        }
                                    } else {
                                        android.util.Log.d("MainActivity", "⏭️ Documento ignorado: type=$type, processed=$processed")
                                    }
                                }
                            }
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
                                try {
                                    locationCallback = startSignificantMovementUpdates(
                                        context,
                                        minDistanceMeters = 50f
                                    ) { lat: Double, lon: Double ->
                                        try {
                                            saveUserLocationWithCoords(context, lat, lon)
                                        } catch (e: Exception) {
                                            android.util.Log.e("MainActivity", "Erro ao salvar localização: ${e.message}")
                                        }
                                    }
                                    isLocationUpdatesActive = true
                                } catch (e: Exception) {
                                    android.util.Log.e("MainActivity", "Erro ao iniciar atualizações de localização: ${e.message}")
                                }
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
                            try {
                                stopLocationUpdates(context, it)
                            } catch (e: Exception) {
                                android.util.Log.e("MainActivity", "Erro ao parar atualizações de localização: ${e.message}")
                            }
                        }
                        notificationListener?.remove()
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
            val firestore = firebaseFirestore
            
            val isEmulator = android.os.Build.FINGERPRINT.contains("generic") ||
                           android.os.Build.MODEL.contains("Emulator") ||
                           android.os.Build.MANUFACTURER.contains("Genymotion")
            
            if (isEmulator) {
                android.util.Log.d("MainActivity", "🔧 Configurando Firebase para emulador")
                try {
                    auth.useEmulator("10.0.2.2", 9099)
                    firestore.useEmulator("10.0.2.2", 8080)
                    android.util.Log.d("MainActivity", "✅ Emulador Firebase configurado")
                } catch (e: Exception) {
                    android.util.Log.w("MainActivity", "⚠️ Erro ao configurar emulador: ${e.message}")
                }
            }
            
            android.util.Log.d("MainActivity", "✅ Firebase inicializado com sucesso")
            
            CoroutineScope(Dispatchers.IO).launch {
                FirebaseDiagnostic.runFullDiagnostic(this@MainActivity)
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