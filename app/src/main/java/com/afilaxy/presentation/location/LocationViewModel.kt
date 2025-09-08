package com.afilaxy.presentation.location

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afilaxy.startSignificantMovementUpdates
import com.afilaxy.stopLocationUpdates
import com.google.android.gms.location.LocationCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LocationViewModel : ViewModel() {
    private var locationCallback: LocationCallback? = null
    private val _location = MutableStateFlow<Pair<Double, Double>?>(null)
    val location: StateFlow<Pair<Double, Double>?> = _location

    fun startLocationUpdates(context: Context) {
        if (locationCallback == null) {
            locationCallback = startSignificantMovementUpdates(context) { lat, lon ->
                viewModelScope.launch {
                    _location.value = lat to lon
                }
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