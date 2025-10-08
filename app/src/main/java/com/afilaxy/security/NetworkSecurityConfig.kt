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
    
    // Secure XML parser configuration to prevent XXE attacks
    fun createSecureXmlParser(): javax.xml.parsers.DocumentBuilderFactory? {
        return try {
            if (!AuthGuard.isUserAuthenticated()) {
                return null
            }
            
            val factory = javax.xml.parsers.DocumentBuilderFactory.newInstance()
            
            // Comprehensive XXE prevention
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false)
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false)
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
            factory.setFeature("http://apache.org/xml/features/validation/schema", false)
            factory.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", true)
            factory.isXIncludeAware = false
            factory.isExpandEntityReferences = false
            factory.isNamespaceAware = false
            
            factory
        } catch (e: Exception) {
            android.util.Log.e("NetworkSecurityConfig", "Failed to create secure XML parser")
            null
        }
    }
}