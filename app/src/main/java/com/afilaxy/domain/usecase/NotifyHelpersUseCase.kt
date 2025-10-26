package com.afilaxy.domain.usecase

import com.afilaxy.domain.model.Emergency
import com.afilaxy.domain.model.Helper
import com.afilaxy.domain.repository.EmergencyRepository
import com.afilaxy.security.AuthGuard
import com.afilaxy.security.RateLimiter
import javax.inject.Inject

class NotifyHelpersUseCase @Inject constructor(
    private val repository: EmergencyRepository,
    private val authGuard: AuthGuard,
    private val rateLimiter: RateLimiter
) {
    
    suspend fun execute(helpers: List<Helper>, emergency: Emergency) {
        // Authentication check
        val user = com.afilaxy.security.AuthGuard.requireAuthentication()
        
        // Rate limiting check
        val userId = user.uid
        if (!rateLimiter.canSendNotification(userId)) {
            throw SecurityException("Rate limit exceeded")
        }
        
        repository.notifyHelpers(helpers, emergency)
    }
}