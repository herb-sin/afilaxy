package com.afilaxy.data.preload

import com.afilaxy.domain.model.Location
import com.afilaxy.domain.repository.EmergencyRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HelperPreloader(
    private val repository: EmergencyRepository
) {
    
    fun preloadNearbyHelpers(userLocation: Location) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                repository.findNearbyHelpers(userLocation)
                android.util.Log.d("HelperPreloader", "Helpers precarregados")
            } catch (e: Exception) {
                android.util.Log.w("HelperPreloader", "Falha no preload")
            }
        }
    }
}