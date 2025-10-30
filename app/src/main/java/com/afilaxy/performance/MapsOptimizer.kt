package com.afilaxy.performance

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import kotlinx.coroutines.delay

/**
 * Otimizações específicas para Google Maps para prevenir ANR
 */
object MapsOptimizer {
    
    /**
     * Configurações otimizadas do mapa para melhor performance
     */
    fun getOptimizedMapProperties(hasLocationPermission: Boolean) = MapProperties(
        isMyLocationEnabled = hasLocationPermission,
        mapType = MapType.NORMAL,
        isTrafficEnabled = false, // Desabilita trânsito para performance
        isBuildingEnabled = false, // Desabilita prédios 3D para performance
        isIndoorEnabled = false // Desabilita mapas internos para performance
    )
    
    /**
     * Configurações otimizadas da UI do mapa
     */
    fun getOptimizedMapUiSettings() = MapUiSettings(
        myLocationButtonEnabled = false,
        zoomControlsEnabled = false,
        compassEnabled = false, // Desabilita bússola para performance
        rotationGesturesEnabled = false, // Desabilita rotação para performance
        tiltGesturesEnabled = false, // Desabilita inclinação para performance
        scrollGesturesEnabled = true, // Mantém scroll
        zoomGesturesEnabled = true // Mantém zoom
    )
    
    /**
     * Executa operação de câmera com delay para prevenir ANR
     */
    suspend fun safeCameraOperation(operation: suspend () -> Unit) {
        try {
            delay(100) // Previne atualizações imediatas que podem causar ANR
            operation()
        } catch (e: Exception) {
            // Log error but don't crash
            android.util.Log.w("MapsOptimizer", "Camera operation failed", e)
        }
    }
    
    /**
     * Valida coordenadas antes de usar no mapa
     */
    fun validateCoordinates(lat: Double, lng: Double): LatLng {
        return if (lat in -90.0..90.0 && lng in -180.0..180.0) {
            LatLng(lat, lng)
        } else {
            LatLng(-23.5505, -46.6333) // São Paulo fallback
        }
    }
    
    /**
     * Gera markers de helpers de forma otimizada
     */
    fun generateOptimizedHelpers(userLocation: LatLng, count: Int = 3): List<LatLng> {
        return (1..count).map { index ->
            val offsetLat = (index * 0.002) * if (index % 2 == 0) 1 else -1
            val offsetLng = (index * 0.001) * if (index % 3 == 0) 1 else -1
            LatLng(
                userLocation.latitude + offsetLat,
                userLocation.longitude + offsetLng
            )
        }
    }
}