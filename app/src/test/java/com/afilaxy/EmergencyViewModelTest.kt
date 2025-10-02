package com.afilaxy

import com.afilaxy.presentation.emergency.EmergencyViewModel
import com.afilaxy.testing.TestingUtils
import org.junit.Test
import org.junit.Assert.*

class EmergencyViewModelTest {
    
    @Test
    fun testLocationUpdate() {
        val viewModel = EmergencyViewModel()
        val mockLocation = TestingUtils.createMockLocation()
        
        viewModel.setLocation(mockLocation)
        
        val state = viewModel.uiState.value
        assertEquals(mockLocation, state.userLocation)
        assertFalse(state.isLoadingLocation)
    }
    
    @Test
    fun testLocationPermission() {
        val viewModel = EmergencyViewModel()
        
        viewModel.updateLocationPermission(true)
        
        assertTrue(viewModel.uiState.value.hasLocationPermission)
    }
}