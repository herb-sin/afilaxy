package com.afilaxy.domain.usecase

import com.afilaxy.domain.model.Emergency
import com.afilaxy.domain.repository.EmergencyRepository
import com.afilaxy.security.AuthGuard
import com.afilaxy.security.AuthProvider
import com.afilaxy.security.InputValidator
import com.afilaxy.security.SecurityUtils
import javax.inject.Inject

class CreateEmergencyUseCase @Inject constructor(
    private val repository: EmergencyRepository,
    private val authProvider: AuthProvider,
    private val inputValidator: InputValidator
) {
    
    suspend fun execute(emergency: Emergency): Result<String> {
        // Authentication check
        if (!authProvider.isUserAuthenticated()) {
            return Result.failure(SecurityException("Authentication required"))
        }
        
        // Input validation
        if (!inputValidator.isValidCoordinate(emergency.location.latitude, emergency.location.longitude)) {
            return Result.failure(IllegalArgumentException("Invalid coordinates"))
        }
        
        return repository.createEmergency(emergency)
    }
}