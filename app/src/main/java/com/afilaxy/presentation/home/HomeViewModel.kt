package com.afilaxy.presentation.home

import android.content.Context
import android.content.pm.PackageManager
import android.Manifest
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
    val errorMessage: String? = null,
    val showLocationDialog: Boolean = false
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
        
    var showLocationDialog by mutableStateOf(false)
        private set
    
    private val auth = FirebaseAuth.getInstance()
    private val context = context
    private val notificationManager = NotificationManager(context)
    private val locationRepository = LocationRepository(context)
    private val helperRepository = HelperRepository()
    private val toggleHelperUseCase = ToggleHelperUseCase(locationRepository, helperRepository)
    private val notificationRepository = NotificationRepository()
    private val sharedPrefs = context.getSharedPreferences("afilaxy_prefs", Context.MODE_PRIVATE)
    
    init {
        // Check auth state without blocking
        auth.currentUser?.let { user ->
            // Simplify - just check if user exists (email verification handled elsewhere)
            isLoggedIn = true
            userEmail = user.email ?: ""
            // Load helper status only if authenticated
            isHelper = sharedPrefs.getBoolean("is_helper", false)
        } ?: run {
            // No user logged in, clear any persisted data
            clearUserData()
        }
        
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
        
        // Se ativando helper, verificar permissão background
        if (newHelperStatus && !hasBackgroundLocationPermission()) {
            showLocationDialog = true
            return
        }
        
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
                }
                is ToggleHelperUseCase.Result.LocationPermissionRequired -> {
                    statusMessage = "Erro: Permissão de localização necessária."
                }
                is ToggleHelperUseCase.Result.LocationNotAvailable -> {
                    statusMessage = "Erro: Não foi possível obter localização. Verifique se o GPS está ativo."
                }
                is ToggleHelperUseCase.Result.NetworkError -> {
                    statusMessage = "Erro de rede. Tente novamente."
                }
                is ToggleHelperUseCase.Result.Error -> {
                    statusMessage = "Erro: ${result.message}"
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
        clearUserData()
        
        statusMessage = "Logout realizado"
    }
    
    private fun clearUserData() {
        isLoggedIn = false
        userEmail = ""
        isHelper = false
        saveHelperStatus(false)
    }
    
    fun clearStatusMessage() {
        statusMessage = ""
    }
    
    private fun hasBackgroundLocationPermission(): Boolean {
        return context.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
    }
    
    fun dismissLocationDialog() {
        showLocationDialog = false
    }
}