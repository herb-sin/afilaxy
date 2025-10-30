package com.afilaxy.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Secure cryptography implementation to prevent CWE-329 (Predictable IV)
 */
object SecureCrypto {
    
    private const val KEYSTORE_ALIAS = "AfilaxySecureKey"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val AES_MODE = "AES/GCM/NoPadding"
    private const val GCM_IV_LENGTH = 12
    private const val GCM_TAG_LENGTH = 16
    
    fun encryptData(plaintext: String): EncryptionResult? {
        return try {
            if (!AuthGuard.isUserAuthenticated()) {
                SecurityMonitor.logThreat("UNAUTHORIZED_ENCRYPTION", "Unauthenticated encryption attempt")
                return null
            }
            
            val secretKey = getOrCreateSecretKey()
            val cipher = Cipher.getInstance(AES_MODE)
            
            // Generate secure random IV for each encryption
            val iv = ByteArray(GCM_IV_LENGTH)
            SecureRandom().nextBytes(iv)
            
            val spec = GCMParameterSpec(GCM_TAG_LENGTH * 8, iv)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec)
            
            val encryptedData = cipher.doFinal(plaintext.toByteArray())
            
            EncryptionResult(encryptedData, iv)
        } catch (e: Exception) {
            SecureLogger.e("SecureCrypto", "Encryption failed", e)
            null
        }
    }
    
    fun decryptData(encryptionResult: EncryptionResult): String? {
        return try {
            if (!AuthGuard.isUserAuthenticated()) {
                SecurityMonitor.logThreat("UNAUTHORIZED_DECRYPTION", "Unauthenticated decryption attempt")
                return null
            }
            
            val secretKey = getOrCreateSecretKey()
            val cipher = Cipher.getInstance(AES_MODE)
            
            val spec = GCMParameterSpec(GCM_TAG_LENGTH * 8, encryptionResult.iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
            
            val decryptedData = cipher.doFinal(encryptionResult.encryptedData)
            String(decryptedData)
        } catch (e: Exception) {
            SecureLogger.e("SecureCrypto", "Decryption failed", e)
            null
        }
    }
    
    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)
        
        return if (keyStore.containsAlias(KEYSTORE_ALIAS)) {
            keyStore.getKey(KEYSTORE_ALIAS, null) as SecretKey
        } else {
            createSecretKey()
        }
    }
    
    private fun createSecretKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            KEYSTORE_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .setUserAuthenticationRequired(false)
            .build()
        
        keyGenerator.init(keyGenParameterSpec)
        return keyGenerator.generateKey()
    }
    
    data class EncryptionResult(
        val encryptedData: ByteArray,
        val iv: ByteArray
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            
            other as EncryptionResult
            
            if (!encryptedData.contentEquals(other.encryptedData)) return false
            if (!iv.contentEquals(other.iv)) return false
            
            return true
        }
        
        override fun hashCode(): Int {
            var result = encryptedData.contentHashCode()
            result = 31 * result + iv.contentHashCode()
            return result
        }
    }
}