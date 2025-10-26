package com.afilaxy.domain.usecase

import com.afilaxy.domain.model.Emergency
import com.afilaxy.domain.model.Location
import com.afilaxy.domain.repository.EmergencyRepository
import com.afilaxy.security.AuthGuard
import com.afilaxy.security.SecurityValidator
import javax.inject.Inject

class CreateEmergencyUseCase @Inject constructor(
    private val repository: EmergencyRepository,
    private val authGuard: AuthGuard,
    private val securityValidator: SecurityValidator
) {
    
    suspend fun execute(location: Location): Emergency {
        // Authentication check
        authGuard.requireAuthentication()
        
        // Input validation
        if (!securityValidator.validateCoordinates(location.latitude, location.longitude)) {
            throw IllegalArgumentException("Invalid coordinates")
        }
        
        return repository.createEmergency(location)
    }
}