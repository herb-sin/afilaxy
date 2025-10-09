package com.afilaxy.utils

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

object GeocodingUtils {
    
    suspend fun getAddressFromCoordinates(
        context: Context,
        latitude: Double,
        longitude: Double
    ): String = withContext(Dispatchers.IO) {
        try {
            if (!Geocoder.isPresent()) {
                return@withContext "Lat: ${String.format("%.4f", latitude)}, Lon: ${String.format("%.4f", longitude)}"
            }
            
            val geocoder = Geocoder(context, Locale.getDefault())
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Para Android 13+ (API 33+)
                var addressResult = "Lat: ${String.format("%.4f", latitude)}, Lon: ${String.format("%.4f", longitude)}"
                
                geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                    if (addresses.isNotEmpty()) {
                        addressResult = formatAddress(addresses[0])
                    }
                }
                
                // Aguarda um pouco para o callback
                kotlinx.coroutines.delay(1000)
                return@withContext addressResult
            } else {
                // Para versões anteriores
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                
                return@withContext if (!addresses.isNullOrEmpty()) {
                    formatAddress(addresses[0])
                } else {
                    "Lat: ${String.format("%.4f", latitude)}, Lon: ${String.format("%.4f", longitude)}"
                }
            }
        } catch (e: Exception) {
            return@withContext "Lat: ${String.format("%.4f", latitude)}, Lon: ${String.format("%.4f", longitude)}"
        }
    }
    
    private fun formatAddress(address: Address): String {
        val addressParts = mutableListOf<String>()
        
        // Rua e número
        address.thoroughfare?.let { street ->
            val streetInfo = if (address.subThoroughfare != null) {
                "$street, ${address.subThoroughfare}"
            } else {
                street
            }
            addressParts.add(streetInfo)
        }
        
        // Bairro
        address.subLocality?.let { neighborhood ->
            addressParts.add(neighborhood)
        }
        
        // Cidade
        address.locality?.let { city ->
            addressParts.add(city)
        }
        
        // Estado
        address.adminArea?.let { state ->
            addressParts.add(state)
        }
        
        return if (addressParts.isNotEmpty()) {
            addressParts.joinToString(", ")
        } else {
            "Endereço não encontrado"
        }
    }
}