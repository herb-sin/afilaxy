package com.afilaxy.performance

import android.content.Context
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapsSdkInitializedCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Otimizador específico para Google Maps
 * Reduz warnings de métodos ocultos e melhora performance
 */
class MapsPerformanceOptimizer : OnMapsSdkInitializedCallback {
    
    companion object {
        private var isInitialized = false
        
        /**
         * Inicializa o Maps SDK de forma otimizada
         */
        suspend fun initializeMaps(context: Context) = withContext(Dispatchers.IO) {
            if (!isInitialized) {
                try {
                    // Inicializa com renderer LATEST para reduzir warnings
                    MapsInitializer.initialize(
                        context.applicationContext,
                        MapsInitializer.Renderer.LATEST,
                        MapsPerformanceOptimizer()
                    )
                    isInitialized = true
                } catch (e: Exception) {
                    LogOptimizer.e("MapsOptimizer", "Erro ao inicializar Maps", e)
                }
            }
        }
        
        /**
         * Configura o mapa para performance otimizada
         */
        fun optimizeMap(googleMap: GoogleMap) {
            try {
                googleMap.apply {
                    // Reduz renderização desnecessária
                    setMapStyle(null)
                    
                    // Otimizações de UI
                    uiSettings.apply {
                        isMapToolbarEnabled = false
                        isIndoorLevelPickerEnabled = false
                        isCompassEnabled = true
                        isMyLocationButtonEnabled = true
                        isZoomControlsEnabled = false
                    }
                    
                    // Configurações de performance
                    setMinZoomPreference(10f)
                    setMaxZoomPreference(20f)
                    
                    // Reduz uso de memória
                    setLatLngBoundsForCameraTarget(null)
                }
            } catch (e: Exception) {
                LogOptimizer.e("MapsOptimizer", "Erro ao otimizar mapa", e)
            }
        }
    }
    
    override fun onMapsSdkInitialized(renderer: MapsInitializer.Renderer) {
        LogOptimizer.d("MapsOptimizer", "Maps SDK inicializado com renderer: $renderer")
    }
}