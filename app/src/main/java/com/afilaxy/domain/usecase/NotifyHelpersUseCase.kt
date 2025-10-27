package com.afilaxy.domain.usecase

import com.afilaxy.domain.model.Emergency
import com.afilaxy.domain.model.Helper
import com.afilaxy.security.AuthGuard
import javax.inject.Inject

class NotifyHelpersUseCase @Inject constructor() {
    
    suspend fun execute(helpers: List<Helper>, emergency: Emergency) {
        if (!AuthGuard.isUserAuthenticated()) {
            throw SecurityException("Authentication required")
        }
        
        // Simplified notification logic
        helpers.forEach { helper ->
            // Send notification to helper
        }
    }
}