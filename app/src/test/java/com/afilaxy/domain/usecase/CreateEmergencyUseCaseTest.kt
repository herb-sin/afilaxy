package com.afilaxy.domain.usecase

import com.afilaxy.domain.model.Emergency
import com.afilaxy.domain.model.Location
import com.afilaxy.domain.repository.EmergencyRepository
import com.afilaxy.security.AuthProvider
import com.afilaxy.security.InputValidator
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class CreateEmergencyUseCaseTest {

    @Mock
    private lateinit var repository: EmergencyRepository
    
    @Mock
    private lateinit var authProvider: AuthProvider
    
    @Mock
    private lateinit var inputValidator: InputValidator

    private lateinit var useCase: CreateEmergencyUseCase

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        useCase = CreateEmergencyUseCase(repository, authProvider, inputValidator)
    }

    @Test
    fun `execute returns success when user is authenticated and location is valid`() = runTest {
        // Arrange
        val emergency = Emergency(
            id = "123",
            userId = "user1",
            userName = "Test User",
            location = Location(10.0, 20.0),
            status = com.afilaxy.domain.model.EmergencyStatus.ACTIVE,
            timestamp = System.currentTimeMillis()
        )
        
        `when`(authProvider.isUserAuthenticated()).thenReturn(true)
        `when`(inputValidator.isValidCoordinate(10.0, 20.0)).thenReturn(true)
        `when`(repository.createEmergency(any())).thenReturn(Result.success("123"))

        // Act
        val result = useCase.execute(emergency)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals("123", result.getOrNull())
        verify(repository).createEmergency(emergency)
    }
    
    @Test
    fun `execute returns failure when user is not authenticated`() = runTest {
        // Arrange
        val emergency = Emergency(
            id = "123",
            userId = "user1",
            userName = "Test User",
            location = Location(10.0, 20.0),
            status = com.afilaxy.domain.model.EmergencyStatus.ACTIVE,
            timestamp = System.currentTimeMillis()
        )
        
        `when`(authProvider.isUserAuthenticated()).thenReturn(false)

        // Act
        val result = useCase.execute(emergency)

        // Assert
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is SecurityException)
        verify(repository, org.mockito.Mockito.never()).createEmergency(any())
    }
    
    @Test
    fun `execute returns failure when location is invalid`() = runTest {
        // Arrange
        val emergency = Emergency(
            id = "123",
            userId = "user1",
            userName = "Test User",
            location = Location(10.0, 20.0),
            status = com.afilaxy.domain.model.EmergencyStatus.ACTIVE,
            timestamp = System.currentTimeMillis()
        )
        
        `when`(authProvider.isUserAuthenticated()).thenReturn(true)
        `when`(inputValidator.isValidCoordinate(10.0, 20.0)).thenReturn(false)

        // Act
        val result = useCase.execute(emergency)

        // Assert
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        verify(repository, org.mockito.Mockito.never()).createEmergency(any())
    }
}
