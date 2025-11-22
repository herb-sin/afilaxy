package com.afilaxy.presentation.location

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afilaxy.startSignificantMovementUpdates
import com.afilaxy.stopLocationUpdates
import com.google.android.gms.location.LocationCallback
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LocationViewModel @Inject constructor() : ViewModel() {
    private var locationCallback: LocationCallback? = null
    private val _location = MutableStateFlow<Pair<Double, Double>?>(null)
    val location: StateFlow<Pair<Double, Double>?> = _location

    fun startLocationUpdates(context: Context) {
        // Desabilitado temporariamente para melhor performance
        android.util.Log.d("LocationViewModel", "Location updates disabled for performance")
    }

    fun stopLocationUpdates(context: Context) {
        locationCallback?.let {
            stopLocationUpdates(context, it)
            locationCallback = null
        }
    }
}