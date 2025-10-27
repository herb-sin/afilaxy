package com.afilaxy.domain.usecase

import com.afilaxy.domain.model.Helper
import com.afilaxy.domain.model.Location
import com.afilaxy.domain.repository.EmergencyRepository
import com.afilaxy.security.AuthGuard
import javax.inject.Inject

class FindHelpersUseCase @Inject constructor(
    private val repository: EmergencyRepository
) {
    
    suspend fun execute(location: Location): List<Helper> {
        if (!AuthGuard.isUserAuthenticated()) {
            throw SecurityException("Authentication required")
        }
        
        if (location.latitude < -90 || location.latitude > 90 || 
            location.longitude < -180 || location.longitude > 180) {
            throw IllegalArgumentException("Invalid coordinates")
        }
        
        return repository.findNearbyHelpers(location, 5.0).getOrElse { emptyList() }
    }
}