package com.afilaxy.domain.usecase

import com.afilaxy.data.repository.HelperRepository
import com.afilaxy.domain.repository.ILocationRepository

class CreateTestHelpersUseCase(
    private val locationRepository: ILocationRepository,
    private val helperRepository: HelperRepository
) {
    
    suspend fun createTestHelpers(): Boolean {
        val location = locationRepository.getCurrentLocation() ?: return false
        
        // Criar 3 helpers de teste em um raio de 200m
        val testHelpers = listOf(
            // Helper 1: ~100m ao norte
            Pair(location.latitude + 0.0009, location.longitude),
            // Helper 2: ~150m ao leste  
            Pair(location.latitude, location.longitude + 0.0013),
            // Helper 3: ~200m ao sudoeste
            Pair(location.latitude - 0.0015, location.longitude - 0.0010)
        )
        
        return try {
            testHelpers.forEachIndexed { index, (lat, lng) ->
                // Simular salvamento de helper de teste
                // Em produção, isso seria feito por usuários reais
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}