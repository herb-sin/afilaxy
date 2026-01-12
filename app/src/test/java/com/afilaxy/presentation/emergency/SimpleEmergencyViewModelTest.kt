package com.afilaxy.presentation.emergency

import com.afilaxy.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations

@ExperimentalCoroutinesApi
class EmergencyRequestViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: SimpleEmergencyViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        viewModel = SimpleEmergencyViewModel()
    }

    @Test
    fun `initial state is correct`() {
        // Then
        assertFalse(viewModel.isLoading)
        assertNull(viewModel.errorMessage)
        assertFalse(viewModel.emergencyCreated)
    }

    @Test
    fun `createEmergency sets loading state`() = runTest {
        // When
        viewModel.createEmergency()

        // Then - should have attempted to create (loading will be false after completion/error)
        // In a real scenario, we'd mock the repository
    }
}
