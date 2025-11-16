package com.afilaxy.presentation.home

import android.app.Application
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.afilaxy.data.EmergencyManager
import com.afilaxy.data.LocationManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class SimpleHomeViewModel(private val application: Application) : AndroidViewModel(application) {
    
    private val auth = FirebaseAuth.getInstance()
    
    var isHelper by mutableStateOf(false)
        private set
    
    var showLocationDialog by mutableStateOf(false)
        private set
    
    val isLoggedIn: Boolean
        get() = auth.currentUser != null
    
    fun toggleHelper() {
        viewModelScope.launch {
            if (isHelper) {
                // Desativar helper
                val success = EmergencyManager.deactivateHelper()
                if (success) {
                    isHelper = false
                }
            } else {
                // Ativar helper com localização real
                android.util.Log.d("SimpleHomeViewModel", "Tentando ativar helper com localização real")
                val success = try {
                    val location = LocationManager.getCurrentLocation(application.applicationContext)
                    android.util.Log.d("SimpleHomeViewModel", "Localização obtida: $location")
                    if (location != null) {
                        android.util.Log.d("SimpleHomeViewModel", "Ativando helper em: ${location.first}, ${location.second}")
                        EmergencyManager.activateHelper(
                            latitude = location.first,
                            longitude = location.second
                        )
                    } else {
                        android.util.Log.e("SimpleHomeViewModel", "Localização é null")
                        false
                    }
                } catch (e: Exception) {
                    android.util.Log.e("SimpleHomeViewModel", "Erro ao obter localização: ${e.message}")
                    false
                }
                if (success) {
                    isHelper = true
                }
            }
        }
    }
    
    fun showLocationDialog() {
        showLocationDialog = true
    }
    
    fun dismissLocationDialog() {
        showLocationDialog = false
    }
    
    fun logout() {
        viewModelScope.launch {
            if (isHelper) {
                EmergencyManager.deactivateHelper()
            }
            auth.signOut()
        }
    }
    
    fun onResume() {
        // Verificar estado real do helper no Firestore
        viewModelScope.launch {
            // TODO: Implementar verificação do estado real do helper
            // Por enquanto, manter o estado atual
            android.util.Log.d("SimpleHomeViewModel", "onResume - Helper status: $isHelper")
        }
    }
}