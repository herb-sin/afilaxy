package com.afilaxy.domain.validator

import android.location.Location

object LocationValidator {
    
    private const val MIN_ACCURACY_METERS = 50f
    private const val MAX_LOCATION_AGE_MS = 30_000L // 30 segundos
    
    // Limites geográficos do Brasil (aproximados)
    private const val BRAZIL_MIN_LAT = -33.75
    private const val BRAZIL_MAX_LAT = 5.27
    private const val BRAZIL_MIN_LNG = -73.99
    private const val BRAZIL_MAX_LNG = -28.84
    
    sealed class ValidationResult {
        object Valid : ValidationResult()
        data class Invalid(val reason: String) : ValidationResult()
    }
    
    fun validateLocation(location: Location): ValidationResult {
        // Verificar precisão
        if (location.accuracy > MIN_ACCURACY_METERS) {
            return ValidationResult.Invalid("Localização imprecisa (${location.accuracy}m)")
        }
        
        // Verificar idade da localização
        val locationAge = System.currentTimeMillis() - location.time
        if (locationAge > MAX_LOCATION_AGE_MS) {
            return ValidationResult.Invalid("Localização desatualizada (${locationAge/1000}s)")
        }
        
        return validateLatLng(location.latitude, location.longitude)
    }
    
    fun validateLatLng(lat: Double, lng: Double): ValidationResult {
        // Verificar coordenadas válidas
        if (!isValidCoordinates(lat, lng)) {
            return ValidationResult.Invalid("Coordenadas inválidas")
        }
        
        // Verificar se está dentro do Brasil
        if (!isWithinBrazil(lat, lng)) {
            return ValidationResult.Invalid("Localização fora da área de cobertura")
        }
        
        return ValidationResult.Valid
    }
    
    private fun isValidCoordinates(lat: Double, lng: Double): Boolean {
        return lat >= -90 && lat <= 90 && lng >= -180 && lng <= 180
    }
    
    private fun isWithinBrazil(lat: Double, lng: Double): Boolean {
        return lat >= BRAZIL_MIN_LAT && lat <= BRAZIL_MAX_LAT &&
               lng >= BRAZIL_MIN_LNG && lng <= BRAZIL_MAX_LNG
    }
}