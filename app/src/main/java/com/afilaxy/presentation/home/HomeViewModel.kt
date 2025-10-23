package com.afilaxy.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.afilaxy.security.AuthGuard
import com.afilaxy.security.InputSanitizer
import com.afilaxy.security.SecureLogger
import com.afilaxy.security.SqlInjectionPrevention
import com.afilaxy.security.ErrorHandler
import com.afilaxy.security.SecurityUtils
import com.google.firebase.firestore.GeoPoint
import com.afilaxy.notification.FCMTokenManager

/**
 * ViewModel for Home screen with comprehensive security measures
 * 
 * Security features:
 * - Input validation and sanitization
 * - Secure error handling
 * - Authentication checks
 * - Safe Firebase operations
 */
class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    // Instâncias Firebase reutilizáveis
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    
    /**
     * Load user data from Firebase with security validation
     */
    fun loadUserData() {
        SecureLogger.d("HomeViewModel", "Starting user data load")
        _uiState.value = _uiState.value.copy(isLoading = true)
        
        viewModelScope.launch {
            try {
                val user = auth.currentUser
                
                if (user != null) {
                    SecureLogger.d("HomeViewModel", "User authenticated, fetching Firestore document")
                    
                    // Validate user ID before using in Firestore query
                    if (!SqlInjectionPrevention.isValidFirebasePath(user.uid)) {
                        SecureLogger.security("LOAD_USER_DATA", "INVALID_USER_ID")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Invalid user session"
                        )
                        return@launch
                    }
                    
                    val userDoc = firestore.collection("users")
                        .document(user.uid)
                        .get()
                        .await()
                    
                    SecureLogger.d("HomeViewModel", "Document exists: ${userDoc.exists()}")
                    
                    val isHelper: Boolean
                    val userName: String
                    
                    if (!userDoc.exists()) {
                        SecureLogger.d("HomeViewModel", "Creating user profile in Firestore")
                        
                        // Validate and sanitize user email before storing
                        val userEmail = user.email
                        val sanitizedEmail = if (userEmail != null && 
                                                InputSanitizer.isValidEmail(userEmail) &&
                                                SqlInjectionPrevention.isValidSqlInput(userEmail)) {
                            userEmail
                        } else {
                            "usuario@exemplo.com"
                        }
                        
                        val userData = mapOf<String, Any>(
                            "name" to sanitizedEmail,
                            "email" to sanitizedEmail,
                            "isHelper" to true,
                            "createdAt" to System.currentTimeMillis()
                        )
                        
                        firestore.collection("users")
                            .document(user.uid)
                            .set(userData)
                            .await()
                        
                        SecureLogger.userAction("CREATE_PROFILE", user.uid, true)
                        
                        isHelper = true
                        userName = sanitizedEmail
                    } else {
                        isHelper = userDoc.getBoolean("isHelper") ?: true
                        
                        // Validate and sanitize stored name
                        val storedName = userDoc.getString("name")
                        userName = if (storedName != null && 
                                      SqlInjectionPrevention.isValidSqlInput(storedName) &&
                                      storedName.length <= 100) {
                            InputSanitizer.sanitizeName(storedName).takeIf { it.isNotBlank() } ?: (user.email ?: "Usuário")
                        } else {
                            user.email ?: "Usuário"
                        }
                    }
                    
                    SecureLogger.d("HomeViewModel", "User data loaded successfully")
                    
                    // Update FCM token
                    try {
                        FCMTokenManager.updateFCMToken()
                    } catch (e: Exception) {
                        SecureLogger.w("HomeViewModel", "FCM token update failed")
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        userName = userName,
                        isHelper = isHelper
                    )
                } else {
                    SecureLogger.security("LOAD_USER_DATA", "USER_NOT_AUTHENTICATED")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Usuário não autenticado"
                    )
                }
            } catch (e: Exception) {
                val error = ErrorHandler.handleFirebaseError(e, "LOAD_USER_DATA")
                SecureLogger.e("HomeViewModel", "Error loading user data", e)
                
                val user = auth.currentUser
                
                // Fallback to offline data with validation
                val fallbackEmail = user?.email
                val safeFallbackName = if (fallbackEmail != null && 
                                          InputSanitizer.isValidEmail(fallbackEmail)) {
                    fallbackEmail
                } else {
                    "Usuário"
                }
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    userName = safeFallbackName,
                    isHelper = true,
                    errorMessage = null
                )
            }
        }
    }
    
    /**
     * Toggle helper status with security validation
     */
    fun toggleHelperStatus() {
        try {
            val currentState = _uiState.value
            val newHelperStatus = !currentState.isHelper
            
            SecureLogger.d("HomeViewModel", "Toggling helper status: $newHelperStatus")
            
            // Update UI immediately for responsiveness
            _uiState.value = _uiState.value.copy(isHelper = newHelperStatus)
            
            viewModelScope.launch {
                try {
                    val user = auth.currentUser
                    if (user != null) {
                        // Validate user ID before Firestore operation
                        if (!SqlInjectionPrevention.isValidFirebasePath(user.uid)) {
                            SecureLogger.security("TOGGLE_HELPER", "INVALID_USER_ID")
                            return@launch
                        }
                        
                        firestore.collection("users")
                            .document(user.uid)
                            .update("isHelper", newHelperStatus)
                            .await()
                        
                        SecureLogger.userAction("TOGGLE_HELPER_STATUS", user.uid, true)
                        SecureLogger.d("HomeViewModel", "Helper status updated in Firestore")
                    } else {
                        SecureLogger.security("TOGGLE_HELPER", "USER_NOT_AUTHENTICATED")
                        SecureLogger.w("HomeViewModel", "User not authenticated - keeping change locally only")
                    }
                } catch (e: Exception) {
                    val error = ErrorHandler.handleFirebaseError(e, "TOGGLE_HELPER_STATUS")
                    SecureLogger.e("HomeViewModel", "Error saving to Firestore", e)
                    SecureLogger.w("HomeViewModel", "Keeping change locally only")
                    // Keep local change even if Firestore fails
                }
            }
        } catch (e: Exception) {
            val error = ErrorHandler.handleException(e, "TOGGLE_HELPER_STATUS")
            SecureLogger.e("HomeViewModel", "Error toggling helper status", e)
        }
    }
    
    /**
     * Update user location with coordinate validation
     */
    fun updateUserLocation(latitude: Double, longitude: Double) {
        try {
            // Validate coordinates before processing
            if (!SecurityUtils.isValidCoordinate(latitude, longitude)) {
                SecureLogger.w("HomeViewModel", "Invalid coordinates provided for location update")
                return
            }
            
            SecureLogger.d("HomeViewModel", "Updating user location")
            
            viewModelScope.launch {
                try {
                    val user = auth.currentUser
                    if (user != null) {
                        // Validate user ID before Firestore operation
                        if (!SqlInjectionPrevention.isValidFirebasePath(user.uid)) {
                            SecureLogger.security("UPDATE_LOCATION", "INVALID_USER_ID")
                            return@launch
                        }
                        
                        val location = GeoPoint(latitude, longitude)
                        
                        firestore.collection("users")
                            .document(user.uid)
                            .update(mapOf(
                                "location" to location,
                                "lastLocationUpdate" to System.currentTimeMillis()
                            ))
                            .await()
                        
                        SecureLogger.d("HomeViewModel", "Location updated in Firestore")
                    } else {
                        SecureLogger.security("UPDATE_LOCATION", "USER_NOT_AUTHENTICATED")
                        SecureLogger.w("HomeViewModel", "User not authenticated - cannot save location")
                    }
                } catch (e: Exception) {
                    val error = ErrorHandler.handleFirebaseError(e, "UPDATE_USER_LOCATION")
                    SecureLogger.e("HomeViewModel", "Error saving location", e)
                }
            }
        } catch (e: Exception) {
            val error = ErrorHandler.handleException(e, "UPDATE_USER_LOCATION")
            SecureLogger.e("HomeViewModel", "Error updating user location", e)
        }
    }
    

}