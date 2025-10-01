package com.afilaxy.presentation.location

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afilaxy.startSignificantMovementUpdates
import com.afilaxy.stopLocationUpdates
import com.google.android.gms.location.LocationCallback
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LocationViewModel : ViewModel() {
    private var locationCallback: LocationCallback? = null
    private val _location = MutableStateFlow<Pair<Double, Double>?>(null)
    val location: StateFlow<Pair<Double, Double>?> = _location

    fun startLocationUpdates(context: Context) {
        // Verificação crítica de autenticação
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        if (user == null) {
            android.util.Log.e("LocationViewModel", "Tentativa de iniciar localização sem autenticação")
            return
        }
        
        if (locationCallback == null) {
            try {
                locationCallback = startSignificantMovementUpdates(context) { lat, lon ->
                    viewModelScope.launch {
                        _location.value = lat to lon
                    }
                }
            } catch (e: SecurityException) {
                android.util.Log.e("LocationViewModel", "Erro de permissão ao iniciar localização: ${e.message}")
            } catch (e: Exception) {
                android.util.Log.e("LocationViewModel", "Erro ao iniciar atualizações de localização: ${e.message}")
            }
        }
    }

    fun stopLocationUpdates(context: Context) {
        locationCallback?.let {
            stopLocationUpdates(context, it)
            locationCallback = null
        }
    }
}