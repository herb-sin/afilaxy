package com.afilaxy.presentation.home

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.afilaxy.data.EmergencyManager
import com.afilaxy.data.LocationManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SimpleHomeViewModel(private val application: Application) : AndroidViewModel(application) {
    
    private val auth = FirebaseAuth.getInstance()
    
    var isHelper by mutableStateOf(false)
        private set
    
    var showLocationDialog by mutableStateOf(false)
        private set
    
    var showPermissionDialog by mutableStateOf(false)
        private set
    
    var permissionMessage by mutableStateOf("")
        private set
    
    val isLoggedIn: Boolean
        get() = auth.currentUser != null
    
    fun toggleHelper() {
        viewModelScope.launch {
            if (isHelper) {
                // Desativar helper
                android.util.Log.d("SimpleHomeViewModel", "Desativando helper manualmente")
                val success = EmergencyManager.deactivateHelper()
                if (success) {
                    isHelper = false
                }
            } else {
                // Verificar permissões antes de ativar
                if (!hasRequiredPermissions()) {
                    showPermissionDialog()
                    return@launch
                }
                
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
                } else {
                    permissionMessage = "Erro ao ativar helper. Verifique suas permissões e conexão."
                    showPermissionDialog = true
                }
            }
        }
    }
    
    private fun hasRequiredPermissions(): Boolean {
        val locationPermission = ContextCompat.checkSelfPermission(
            application.applicationContext,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        
        val notificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                application.applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
        
        android.util.Log.d("SimpleHomeViewModel", "Permissões - Localização: $locationPermission, Notificação: $notificationPermission")
        
        if (!locationPermission) {
            permissionMessage = "Permissão de localização necessária para ser um helper."
            return false
        }
        
        if (!notificationPermission) {
            permissionMessage = "Permissão de notificação necessária para receber alertas de emergência."
            return false
        }
        
        return true
    }
    
    private fun showPermissionDialog() {
        showPermissionDialog = true
    }
    
    fun showLocationDialog() {
        showLocationDialog = true
    }
    
    fun dismissLocationDialog() {
        showLocationDialog = false
    }
    
    fun dismissPermissionDialog() {
        showPermissionDialog = false
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
            checkHelperStatusInFirestore()
        }
    }
    
    private suspend fun checkHelperStatusInFirestore() {
        try {
            val userId = auth.currentUser?.uid ?: return
            val firestore = FirebaseFirestore.getInstance()
            
            val helperDoc = firestore.collection("helpers")
                .document(userId)
                .get()
                .await()
            
            val isActiveInFirestore = helperDoc.exists() && helperDoc.getBoolean("isActive") == true
            
            if (isHelper != isActiveInFirestore) {
                android.util.Log.d("SimpleHomeViewModel", "Sincronizando estado: local=$isHelper, firestore=$isActiveInFirestore")
                isHelper = isActiveInFirestore
            }
        } catch (e: Exception) {
            android.util.Log.e("SimpleHomeViewModel", "Erro ao verificar status no Firestore: ${e.message}")
        }
    }
    

}