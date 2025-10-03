package com.afilaxy.domain.usecase

import com.afilaxy.domain.model.Helper
import com.afilaxy.domain.model.Location
import com.afilaxy.domain.repository.EmergencyRepository

class FindHelpersUseCase(
    private val repository: EmergencyRepository
) {
    
    suspend operator fun invoke(location: Location): Result<List<Helper>> {
        return try {
            val helpers = repository.findNearbyHelpers(location)
            Result.success(helpers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}