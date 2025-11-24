package com.afilaxy.domain.usecase

import com.afilaxy.domain.model.Helper
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
import org.mockito.kotlin.verify

class FindHelpersUseCaseTest {

    @Mock
    private lateinit var repository: EmergencyRepository
    
    @Mock
    private lateinit var authProvider: AuthProvider
    
    @Mock
    private lateinit var inputValidator: InputValidator

    private lateinit var useCase: FindHelpersUseCase

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        useCase = FindHelpersUseCase(repository, authProvider, inputValidator)
    }

    @Test
    fun `execute returns helpers when user is authenticated and location is valid`() = runTest {
        // Arrange
        val location = Location(10.0, 20.0)
        val helpers = listOf(
            Helper(id = "1", name = "Helper 1", latitude = 10.1, longitude = 20.1, isActive = true),
            Helper(id = "2", name = "Helper 2", latitude = 10.2, longitude = 20.2, isActive = true)
        )
        
        `when`(authProvider.isUserAuthenticated()).thenReturn(true)
        `when`(inputValidator.isValidCoordinate(10.0, 20.0)).thenReturn(true)
        `when`(repository.findNearbyHelpers(any(), any())).thenReturn(Result.success(helpers))

        // Act
        val result = useCase.execute(location)

        // Assert
        assertEquals(2, result.size)
        assertEquals("Helper 1", result[0].name)
        verify(repository).findNearbyHelpers(location, 5.0)
    }
    
    @Test(expected = SecurityException::class)
    fun `execute throws SecurityException when user is not authenticated`() = runTest {
        // Arrange
        val location = Location(10.0, 20.0)
        `when`(authProvider.isUserAuthenticated()).thenReturn(false)

        // Act
        useCase.execute(location)
    }
    
    @Test(expected = IllegalArgumentException::class)
    fun `execute throws IllegalArgumentException when location is invalid`() = runTest {
        // Arrange
        val location = Location(10.0, 20.0)
        `when`(authProvider.isUserAuthenticated()).thenReturn(true)
        `when`(inputValidator.isValidCoordinate(10.0, 20.0)).thenReturn(false)

        // Act
        useCase.execute(location)
    }
}
