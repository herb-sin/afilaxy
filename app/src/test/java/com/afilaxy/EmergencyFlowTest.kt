package com.afilaxy

import com.afilaxy.domain.model.Location
import com.afilaxy.domain.repository.EmergencyRepositoryImpl
import com.afilaxy.domain.usecase.CreateEmergencyUseCase
import com.afilaxy.domain.usecase.FindHelpersUseCase
import com.afilaxy.testing.TestDataGenerator
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.Assert.*

class EmergencyFlowTest {
    
    private val repository = EmergencyRepositoryImpl()
    private val createEmergencyUseCase = CreateEmergencyUseCase(repository)
    private val findHelpersUseCase = FindHelpersUseCase(repository)
    
    @Test
    fun `test emergency creation flow`() = runBlocking {
        val location = Location(-23.5505, -46.6333)
        
        val result = createEmergencyUseCase(location)
        
        assertTrue("Emergency should be created successfully", result.isSuccess)
        result.getOrNull()?.let { emergency ->
            assertNotNull("Emergency ID should not be null", emergency.id)
            assertEquals("Location should match", location, emergency.location)
        }
    }
    
    @Test
    fun `test helper search with cache`() = runBlocking {
        val location = Location(-23.5505, -46.6333)
        
        // Primeira busca (sem cache)
        val firstResult = findHelpersUseCase(location)
        assertTrue("First search should succeed", firstResult.isSuccess)
        
        // Segunda busca (com cache)
        val secondResult = findHelpersUseCase(location)
        assertTrue("Cached search should succeed", secondResult.isSuccess)
        
        assertEquals("Results should be identical", 
            firstResult.getOrNull()?.size, 
            secondResult.getOrNull()?.size)
    }
    
    @Test
    fun `test data generator`() {
        val emergencies = TestDataGenerator.generateEmergencies(5, "São Paulo, SP")
        val helpers = TestDataGenerator.generateHelpers(3, "São Paulo, SP")
        
        assertEquals("Should generate 5 emergencies", 5, emergencies.size)
        assertEquals("Should generate 3 helpers", 3, helpers.size)
        
        emergencies.forEach { emergency ->
            assertNotNull("Emergency should have valid ID", emergency.id)
            assertNotNull("Emergency should have valid location", emergency.location)
        }
    }
}