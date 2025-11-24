package com.afilaxy.domain.usecase

import com.afilaxy.domain.model.Helper
import com.afilaxy.domain.model.Location
import com.afilaxy.domain.repository.EmergencyRepository
import com.afilaxy.security.AuthProvider
import com.afilaxy.security.InputValidator
import javax.inject.Inject

class FindHelpersUseCase @Inject constructor(
    private val repository: EmergencyRepository,
    private val authProvider: AuthProvider,
    private val inputValidator: InputValidator
) {
    
    suspend fun execute(location: Location): List<Helper> {
        if (!authProvider.isUserAuthenticated()) {
            throw SecurityException("Authentication required")
        }
        
        if (!inputValidator.isValidCoordinate(location.latitude, location.longitude)) {
            throw IllegalArgumentException("Invalid coordinates")
        }
        
        return repository.findNearbyHelpers(location, 5.0).getOrElse { emptyList() }
    }
}