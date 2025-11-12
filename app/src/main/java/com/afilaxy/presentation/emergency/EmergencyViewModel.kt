package com.afilaxy.presentation.emergency

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import com.afilaxy.data.repository.LocationRepository
import com.afilaxy.data.repository.HelperRepository
import com.afilaxy.data.repository.EmergencyRequestRepository
import com.afilaxy.data.NotificationRepository
import com.afilaxy.domain.usecase.RequestEmergencyHelpUseCase
import com.afilaxy.domain.model.Helper
import kotlinx.coroutines.launch

class EmergencyViewModel(application: Application) : AndroidViewModel(application) {
    
    var emergencyActive by mutableStateOf(false)
        private set
        
    var helpersFound by mutableStateOf(0)
        private set
        
    var nearbyHelpers by mutableStateOf<List<Helper>>(emptyList())
        private set
        
    var currentRequestId by mutableStateOf<String?>(null)
        private set
        
    var userLocation by mutableStateOf("")
        private set
        
    var isLoading by mutableStateOf(false)
        private set
        
    var statusMessage by mutableStateOf("")
        private set
        
    var timeRemaining by mutableStateOf(0)
        private set
        
    private var timerJob: Job? = null
    
    private val locationRepository = LocationRepository(getApplication())
    private val helperRepository = HelperRepository()
    private val emergencyRequestRepository = EmergencyRequestRepository()
    private val notificationRepository = NotificationRepository()
    private val requestEmergencyHelpUseCase = RequestEmergencyHelpUseCase(
        locationRepository, helperRepository, emergencyRequestRepository, notificationRepository
    )
    
    init {
        getCurrentLocation()
    }
    
    private fun getCurrentLocation() {
        viewModelScope.launch {
            statusMessage = "Obtendo localização GPS..."
            
            try {
                val location = locationRepository.getCurrentLocation()
                
                location?.let {
                    userLocation = "${it.latitude}, ${it.longitude}"
                    statusMessage = "📍 GPS: ${String.format("%.4f", it.latitude)}, ${String.format("%.4f", it.longitude)}"
                } ?: run {
                    statusMessage = "GPS indisponível. Ative a localização para usar o app."
                }
            } catch (e: Exception) {
                android.util.Log.e("EmergencyViewModel", "GPS error: ${e.javaClass.simpleName}")
                statusMessage = "Erro GPS. Ative a localização para continuar."
            }
        }
    }
    
    fun requestHelp() {
        isLoading = true
        statusMessage = "Enviando pedido de ajuda..."
        
        viewModelScope.launch {
            val result = requestEmergencyHelpUseCase.execute()
            
            when (result) {
                is RequestEmergencyHelpUseCase.Result.Success -> {
                    emergencyActive = true
                    currentRequestId = result.requestId
                    nearbyHelpers = result.helpers
                    helpersFound = result.helpers.size
                    statusMessage = "${result.helpers.size} helpers encontrados! Notificações enviadas."
                    
                    // Desativar helper status (não pode ser helper enquanto pede ajuda)
                    getSecurePreferences().edit().putBoolean("is_helper", false).apply()
                    
                    startTimer()
                }
                is RequestEmergencyHelpUseCase.Result.LocationInvalid -> {
                    statusMessage = "Localização inválida: ${result.reason}"
                }
                is RequestEmergencyHelpUseCase.Result.LocationPermissionRequired -> {
                    statusMessage = "Permissão de localização necessária."
                }
                is RequestEmergencyHelpUseCase.Result.LocationNotAvailable -> {
                    statusMessage = "Não foi possível obter localização. Verifique se o GPS está ativo."
                }
                is RequestEmergencyHelpUseCase.Result.NoHelpersFound -> {
                    emergencyActive = true
                    helpersFound = 0
                    statusMessage = "Nenhum helper próximo encontrado. Tente novamente."
                    
                    // Desativar helper status mesmo sem helpers encontrados
                    getSecurePreferences().edit().putBoolean("is_helper", false).apply()
                }
                is RequestEmergencyHelpUseCase.Result.Error -> {
                    statusMessage = "Erro: ${result.message}"
                }
            }
            
            isLoading = false
        }
    }
    
    fun cancelHelp() {
        viewModelScope.launch {
            currentRequestId?.let { requestId ->
                val success = emergencyRequestRepository.cancelEmergencyRequest(requestId)
                if (success) {
                    statusMessage = "Solicitação cancelada com sucesso"
                } else {
                    statusMessage = "Erro ao cancelar solicitação"
                }
            }
            
            emergencyActive = false
            currentRequestId = null
            helpersFound = 0
            nearbyHelpers = emptyList()
            stopTimer()
        }
    }
    
    private fun startTimer() {
        timeRemaining = 300 // 5 minutos em segundos
        timerJob = viewModelScope.launch {
            while (timeRemaining > 0 && emergencyActive) {
                delay(1000)
                timeRemaining--
                
                if (timeRemaining == 0) {
                    // Pedido expirou automaticamente
                    emergencyActive = false
                    statusMessage = "Pedido expirou após 5 minutos"
                }
            }
        }
    }
    
    private fun stopTimer() {
        timerJob?.cancel()
        timeRemaining = 0
    }
    
    fun refreshLocation() {
        getCurrentLocation()
    }
    
    private fun getSecurePreferences() = try {
        androidx.security.crypto.EncryptedSharedPreferences.create(
            "afilaxy_prefs",
            androidx.security.crypto.MasterKeys.getOrCreate(androidx.security.crypto.MasterKeys.AES256_GCM_SPEC),
            getApplication(),
            androidx.security.crypto.EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            androidx.security.crypto.EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        getApplication<Application>().getSharedPreferences("afilaxy_prefs", Application.MODE_PRIVATE)
    }
}

