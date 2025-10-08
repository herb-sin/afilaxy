package com.afilaxy.biometric

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BiometricAuthManager @Inject constructor() {
    
    fun isBiometricAvailable(activity: FragmentActivity): Boolean {
        val biometricManager = BiometricManager.from(activity)
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }
    
    fun authenticateForEmergency(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (!isBiometricAvailable(activity)) {
            onSuccess() // Fallback to normal flow
            return
        }
        
        val biometricPrompt = BiometricPrompt(
            activity,
            ContextCompat.getMainExecutor(activity),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }
                
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    if (errorCode == BiometricPrompt.ERROR_USER_CANCELED || 
                        errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                        onSuccess() // Allow emergency to proceed
                    } else {
                        onError(errString.toString())
                    }
                }
                
                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    // Don't block emergency, just log
                }
            }
        )
        
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Confirmar Emergência")
            .setSubtitle("Use sua biometria para confirmar a solicitação de emergência")
            .setNegativeButtonText("Pular")
            .build()
        
        biometricPrompt.authenticate(promptInfo)
    }
}