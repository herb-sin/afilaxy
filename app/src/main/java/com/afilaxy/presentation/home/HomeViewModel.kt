package com.afilaxy.presentation.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.afilaxy.notification.NotificationManager
import com.afilaxy.data.repository.HelperRepository
import com.afilaxy.data.repository.LocationRepository
import com.afilaxy.data.NotificationRepository
import com.afilaxy.domain.usecase.ToggleHelperUseCase
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

data class HomeUiState(
    val isHelper: Boolean = false,
    val errorMessage: String? = null
)

class HomeViewModel(context: Context) : ViewModel() {
    
    var isHelper by mutableStateOf(false)
        private set
        
    var isLoggedIn by mutableStateOf(false)
        private set
        
    var userEmail by mutableStateOf("")
        private set
        
    var statusMessage by mutableStateOf("")
        private set
    
    private val auth = FirebaseAuth.getInstance()
    private val notificationManager = NotificationManager(context)
    private val locationRepository = LocationRepository(context)
    private val helperRepository = HelperRepository()
    private val toggleHelperUseCase = ToggleHelperUseCase(locationRepository, helperRepository)
    private val notificationRepository = NotificationRepository()
    private val sharedPrefs = context.getSharedPreferences("afilaxy_prefs", Context.MODE_PRIVATE)
    
    init {
        // Check auth state without blocking
        auth.currentUser?.let { user ->
            isLoggedIn = true
            userEmail = user.email ?: ""
        }
        
        // Load helper status from SharedPreferences
        isHelper = sharedPrefs.getBoolean("is_helper", false)
        
        // Initialize notifications and save FCM token
        viewModelScope.launch {
            notificationManager.initializeNotifications()
            
            // Save FCM token if user is logged in
            auth.currentUser?.uid?.let { userId ->
                notificationRepository.saveUserToken(userId)
            }
        }
    }
    
    fun toggleHelper() {
        val newHelperStatus = !isHelper
        
        viewModelScope.launch {
            val result = if (newHelperStatus) {
                toggleHelperUseCase.activateHelper()
            } else {
                toggleHelperUseCase.deactivateHelper()
            }
            
            when (result) {
                is ToggleHelperUseCase.Result.Success -> {
                    isHelper = newHelperStatus
                    saveHelperStatus(newHelperStatus)
                    // Status refletido dinamicamente na UI
                }
                is ToggleHelperUseCase.Result.LocationPermissionRequired -> {
                    statusMessage = "Erro: Permissão de localização necessária."
                    // Manter o estado anterior em caso de erro
                }
                is ToggleHelperUseCase.Result.LocationNotAvailable -> {
                    statusMessage = "Erro: Não foi possível obter localização. Verifique se o GPS está ativo."
                    // Manter o estado anterior em caso de erro
                }
                is ToggleHelperUseCase.Result.NetworkError -> {
                    statusMessage = "Erro de rede. Tente novamente."
                    // Manter o estado anterior em caso de erro
                }
                is ToggleHelperUseCase.Result.Error -> {
                    statusMessage = "Erro: ${result.message}"
                    // Manter o estado anterior em caso de erro
                }
            }
        }
    }
    
    private fun saveHelperStatus(status: Boolean) {
        sharedPrefs.edit().putBoolean("is_helper", status).apply()
    }
    
    fun quickLogin() {
        // Simple test login - bypass Firebase for test credentials
        isLoggedIn = true
        userEmail = "test@test.com"
    }
    
    fun logout() {
        // Remove helper status before logout
        if (isHelper) {
            viewModelScope.launch {
                helperRepository.removeHelper()
            }
        }
        
        auth.signOut()
        isLoggedIn = false
        userEmail = ""
        isHelper = false
        
        // Clear helper status from SharedPreferences
        saveHelperStatus(false)
        
        statusMessage = "Logout realizado"
    }
    
    fun clearStatusMessage() {
        statusMessage = ""
    }
}