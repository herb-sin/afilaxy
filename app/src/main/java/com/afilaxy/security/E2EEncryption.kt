package com.afilaxy.security

import android.util.Base64
import java.security.*
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher

object E2EEncryption {
    
    private const val ALGORITHM = "RSA"
    private const val TRANSFORMATION = "RSA/ECB/PKCS1Padding"
    private const val KEY_SIZE = 2048
    
    fun generateKeyPair(): KeyPair {
        val keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM)
        keyPairGenerator.initialize(KEY_SIZE)
        return keyPairGenerator.generateKeyPair()
    }
    
    fun encryptMessage(message: String, publicKeyString: String): String? {
        return try {
            val publicKeyBytes = Base64.decode(publicKeyString, Base64.DEFAULT)
            val keySpec = X509EncodedKeySpec(publicKeyBytes)
            val keyFactory = KeyFactory.getInstance(ALGORITHM)
            val publicKey = keyFactory.generatePublic(keySpec)
            
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, publicKey)
            
            val encryptedBytes = cipher.doFinal(message.toByteArray())
            Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
        } catch (e: Exception) {
            SecurityUtils.safeLog("E2EEncryption", "Encryption failed: ${e.message}", SecurityUtils.LogLevel.ERROR)
            null
        }
    }
    
    fun decryptMessage(encryptedMessage: String, privateKey: PrivateKey): String? {
        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, privateKey)
            
            val encryptedBytes = Base64.decode(encryptedMessage, Base64.DEFAULT)
            val decryptedBytes = cipher.doFinal(encryptedBytes)
            String(decryptedBytes)
        } catch (e: Exception) {
            SecurityUtils.safeLog("E2EEncryption", "Decryption failed: ${e.message}", SecurityUtils.LogLevel.ERROR)
            null
        }
    }
    
    fun publicKeyToString(publicKey: PublicKey): String {
        return Base64.encodeToString(publicKey.encoded, Base64.DEFAULT)
    }
}