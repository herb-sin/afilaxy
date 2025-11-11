package com.afilaxy.domain.usecase

import com.afilaxy.data.repository.HelperRepository
import com.afilaxy.domain.repository.ILocationRepository

class ToggleHelperUseCase(
    private val locationRepository: ILocationRepository,
    private val helperRepository: HelperRepository
) {
    
    sealed class Result {
        object Success : Result()
        object LocationPermissionRequired : Result()
        object LocationNotAvailable : Result()
        object NetworkError : Result()
        data class Error(val message: String) : Result()
    }
    
    suspend fun activateHelper(): Result {
        android.util.Log.d("ToggleHelperUseCase", "Ativando helper...")
        
        if (!locationRepository.hasLocationPermission()) {
            android.util.Log.w("ToggleHelperUseCase", "Permissão de localização não concedida")
            return Result.LocationPermissionRequired
        }
        
        val location = locationRepository.getCurrentLocation()
        if (location == null) {
            android.util.Log.w("ToggleHelperUseCase", "Localização não disponível")
            return Result.LocationNotAvailable
        }
        
        android.util.Log.d("ToggleHelperUseCase", "Salvando helper em (${location.latitude}, ${location.longitude})")
        
        return if (helperRepository.saveHelper(location.latitude, location.longitude)) {
            android.util.Log.d("ToggleHelperUseCase", "Helper ativado com sucesso")
            Result.Success
        } else {
            android.util.Log.e("ToggleHelperUseCase", "Erro ao salvar helper")
            Result.NetworkError
        }
    }
    
    suspend fun deactivateHelper(): Result {
        return if (helperRepository.removeHelper()) {
            Result.Success
        } else {
            Result.NetworkError
        }
    }
}