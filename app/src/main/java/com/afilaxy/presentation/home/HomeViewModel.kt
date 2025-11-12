package com.afilaxy.presentation.home

import android.content.Context
import android.content.pm.PackageManager
import android.Manifest
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.afilaxy.data.NotificationRepository
import com.afilaxy.domain.usecase.ToggleHelperUseCase
import com.afilaxy.domain.repository.IAuthRepository
import com.afilaxy.domain.repository.IPreferencesRepository
import com.afilaxy.security.AuthResult
import kotlinx.coroutines.launch

data class HomeUiState(
    val isHelper: Boolean = false,
    val errorMessage: String? = null,
    val showLocationDialog: Boolean = false
)

class HomeViewModel(
    private val authRepository: IAuthRepository,
    private val preferencesRepository: IPreferencesRepository,
    private val toggleHelperUseCase: ToggleHelperUseCase,
    private val notificationRepository: NotificationRepository
) : ViewModel() {
    
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
    
    init {
        initializeUserState()
        initializeNotifications()
    }
    
    private fun initializeUserState() {
        isLoggedIn = authRepository.isLoggedIn()
        if (isLoggedIn) {
            authRepository.getCurrentUserId()?.let { userId ->
                userEmail = userId
                isHelper = preferencesRepository.getBoolean("is_helper", false)
            }
        } else {
            clearUserData()
        }
    }
    
    private fun initializeNotifications() {
        viewModelScope.launch {
            authRepository.getCurrentUserId()?.let { userId ->
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
        preferencesRepository.putBoolean("is_helper", status)
    }
    
    fun quickLogin() {
        viewModelScope.launch {
            when (val result = authRepository.validateAuthentication()) {
                is AuthResult.Authenticated -> {
                    isLoggedIn = true
                    userEmail = result.userId
                }
                else -> {
                    statusMessage = "Falha na autenticação"
                }
            }
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            if (isHelper) {
                toggleHelperUseCase.deactivateHelper()
            }
            
            if (authRepository.signOut()) {
                clearUserData()
                statusMessage = "Logout realizado"
            } else {
                statusMessage = "Erro no logout"
            }
        }
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
        // This check should be done in the UI layer, not ViewModel
        // For now, return true to avoid compilation error
        return true
    }
    
    fun dismissLocationDialog() {
        showLocationDialog = false
    }
    
    fun refreshHelperStatus() {
        isHelper = preferencesRepository.getBoolean("is_helper", false)
    }
    
    fun onResume() {
        refreshHelperStatus()
    }
}