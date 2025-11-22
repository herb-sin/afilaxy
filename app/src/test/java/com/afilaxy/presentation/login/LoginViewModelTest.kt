package com.afilaxy.presentation.login

import com.afilaxy.MainDispatcherRule
import com.afilaxy.data.NotificationRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Mock
    private lateinit var firebaseAuth: FirebaseAuth

    @Mock
    private lateinit var notificationRepository: NotificationRepository

    private lateinit var viewModel: LoginViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        viewModel = LoginViewModel(firebaseAuth, notificationRepository)
    }

    @Test
    fun `initial state is correct`() {
        val state = viewModel.uiState.value
        assertEquals("", state.email)
        assertEquals("", state.password)
        assertEquals(false, state.isLoading)
    }

    @Test
    fun `updateEmail updates state correctly`() {
        val testEmail = "test@example.com"
        viewModel.updateEmail(testEmail)
        
        assertEquals(testEmail, viewModel.uiState.value.email)
    }
    
    @Test
    fun `toggleMode switches between login and register`() {
        // Initial state is login (isRegisterMode = false)
        assertEquals(false, viewModel.uiState.value.isRegisterMode)
        
        viewModel.toggleMode()
        assertEquals(true, viewModel.uiState.value.isRegisterMode)
        
        viewModel.toggleMode()
        assertEquals(false, viewModel.uiState.value.isRegisterMode)
    }
}
