package com.afilaxy.domain.usecase

import com.afilaxy.domain.model.Emergency
import com.afilaxy.domain.model.Location
import com.afilaxy.domain.repository.EmergencyRepository
import com.afilaxy.security.AuthValidator
import com.afilaxy.security.RateLimiter

class CreateEmergencyUseCase(
    private val repository: EmergencyRepository
) {
    
    suspend operator fun invoke(location: Location): Result<Emergency> {
        return try {
            val user = AuthValidator.requireVerifiedEmail()
            
            if (!RateLimiter.canCreateEmergency(user.uid)) {
                val remainingTime = RateLimiter.getRemainingTime(user.uid, "emergency")
                return Result.failure(Exception("Aguarde ${remainingTime / 1000}s antes de criar nova emergência"))
            }
            
            val emergency = repository.createEmergency(location)
            Result.success(emergency)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}