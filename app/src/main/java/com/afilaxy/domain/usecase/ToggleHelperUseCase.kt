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
        return try {
            if (!locationRepository.hasLocationPermission()) {
                return Result.LocationPermissionRequired
            }
            
            val location = locationRepository.getCurrentLocation()
                ?: return Result.LocationNotAvailable
            
            if (helperRepository.saveHelper(location.latitude, location.longitude)) {
                Result.Success
            } else {
                Result.NetworkError
            }
        } catch (e: Exception) {
            android.util.Log.e("ToggleHelperUseCase", "Error activating helper: ${e.javaClass.simpleName}")
            Result.Error("Failed to activate helper")
        }
    }
    
    suspend fun deactivateHelper(): Result {
        return try {
            if (helperRepository.removeHelper()) {
                Result.Success
            } else {
                Result.NetworkError
            }
        } catch (e: Exception) {
            android.util.Log.e("ToggleHelperUseCase", "Error deactivating helper: ${e.javaClass.simpleName}")
            Result.Error("Failed to deactivate helper")
        }
    }
}