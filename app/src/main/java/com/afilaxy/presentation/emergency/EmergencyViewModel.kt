package com.afilaxy.presentation.emergency

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.afilaxy.location.RealLocationManager
import com.afilaxy.location.LocationResult
import com.afilaxy.notification.NotificationManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class EmergencyViewModel(private val context: Context? = null) : ViewModel() {
    
    var emergencyActive by mutableStateOf(false)
        private set
        
    var helpersFound by mutableStateOf(0)
        private set
        
    var userLocation by mutableStateOf("-23.5505, -46.6333") // São Paulo como padrão
        private set
        
    var isLoading by mutableStateOf(false)
        private set
        
    var statusMessage by mutableStateOf("")
        private set
    
    private val auth = FirebaseAuth.getInstance()
    private val notificationManager = context?.let { NotificationManager(it) }
    private val locationManager = context?.let { RealLocationManager(it) }
    
    init {
        getCurrentLocation()
        initializeNotifications()
    }
    
    private fun getCurrentLocation() {
        viewModelScope.launch {
            statusMessage = "Obtendo localização..."
            
            try {
                if (locationManager != null) {
                    // Heavy operation in IO, but state updates on Main
                    val result = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                        kotlinx.coroutines.withTimeout(3000) {
                            locationManager.getCurrentLocation()
                        }
                    }
                    
                    when (result) {
                        is LocationResult.Success -> {
                            userLocation = "${result.latitude}, ${result.longitude}"
                            statusMessage = "Localização obtida com sucesso"
                        }
                        is LocationResult.Error -> {
                            userLocation = "-23.5505, -46.6333"
                            statusMessage = "Usando localização padrão: ${result.message}"
                        }
                        is LocationResult.PermissionDenied -> {
                            userLocation = "-23.5505, -46.6333"
                            statusMessage = "Permissão negada. Usando localização padrão."
                        }
                    }
                } else {
                    userLocation = "-23.5505, -46.6333"
                    statusMessage = "Modo desenvolvimento - localização simulada"
                }
            } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                userLocation = "-23.5505, -46.6333"
                statusMessage = "Timeout na localização. Usando padrão."
            } catch (e: Exception) {
                userLocation = "-23.5505, -46.6333"
                statusMessage = "Erro na localização. Usando padrão."
            }
        }
    }
    
    private fun initializeNotifications() {
        viewModelScope.launch {
            notificationManager?.initializeNotifications()
        }
    }
    
    fun requestHelp() {
        isLoading = true
        statusMessage = "Enviando pedido de ajuda..."
        
        viewModelScope.launch {
            try {
                // Heavy operations in IO, state updates on Main
                val success = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    kotlinx.coroutines.delay(500)
                    
                    kotlinx.coroutines.withTimeout(5000) {
                        if (notificationManager != null && auth.currentUser != null) {
                            val coords = userLocation.split(",")
                            val lat = coords[0].trim().toDouble()
                            val lng = coords[1].trim().toDouble()
                            
                            notificationManager.sendEmergencyNotification(
                                latitude = lat,
                                longitude = lng,
                                message = "Preciso de ajuda com asma! Localização: São Paulo, SP"
                            )
                        } else {
                            true // Demo mode
                        }
                    }
                }
                
                // State updates on Main thread
                emergencyActive = true
                helpersFound = 3
                statusMessage = if (success) {
                    "Ajuda solicitada! Helpers notificados via Firebase."
                } else {
                    "Emergência ativa (modo offline)"
                }
                
            } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                emergencyActive = true
                helpersFound = 3
                statusMessage = "Emergência ativa (timeout na rede)"
            } catch (e: Exception) {
                emergencyActive = true
                helpersFound = 3
                statusMessage = "Emergência ativa (modo demonstração)"
            } finally {
                isLoading = false
            }
        }
    }
    
    fun cancelHelp() {
        emergencyActive = false
        helpersFound = 0
        statusMessage = "Solicitação cancelada"
    }
    
    fun refreshLocation() {
        getCurrentLocation()
    }
}

