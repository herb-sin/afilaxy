package com.afilaxy.domain.usecase

import com.afilaxy.domain.model.Emergency
import com.afilaxy.domain.repository.EmergencyRepository
import com.afilaxy.security.AuthGuard
import com.afilaxy.security.SecurityUtils
import javax.inject.Inject

class CreateEmergencyUseCase @Inject constructor(
    private val repository: EmergencyRepository
) {
    
    suspend fun execute(emergency: Emergency): Result<String> {
        // Authentication check
        if (!AuthGuard.isUserAuthenticated()) {
            return Result.failure(SecurityException("Authentication required"))
        }
        
        // Input validation
        if (!SecurityUtils.isValidCoordinate(emergency.location.latitude, emergency.location.longitude)) {
            return Result.failure(IllegalArgumentException("Invalid coordinates"))
        }
        
        return repository.createEmergency(emergency)
    }
}