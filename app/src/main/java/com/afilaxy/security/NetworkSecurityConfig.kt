package com.afilaxy.security

import com.afilaxy.utils.ErrorHandler
import com.google.firebase.auth.FirebaseAuth
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object NetworkSecurityConfig {
    
    fun createSecureHttpClient(): OkHttpClient? {
        return ErrorHandler.safeOperation {
            if (!isUserAuthenticated()) {
                throw SecurityException("User must be authenticated")
            }
            
            // Secure certificate pinning - production certificates should be used
            val certificatePinner = CertificatePinner.Builder()
                .add("googleapis.com", "sha256/WoiWRyIOVNa9ihaBciRSC7XHjliYS9VwUGOIud4PB18=")
                .add("firebaseio.com", "sha256/WoiWRyIOVNa9ihaBciRSC7XHjliYS9VwUGOIud4PB18=")
                .build()
            
            OkHttpClient.Builder()
                .certificatePinner(certificatePinner)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()
        }
    }
    
    private fun isUserAuthenticated(): Boolean {
        return AuthGuard.isUserAuthenticated()
    }
    

}