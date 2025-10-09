package com.afilaxy.security

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object SecureBackup {
    
    private const val ALGORITHM = "AES"
    private const val TRANSFORMATION = "AES/CBC/PKCS5Padding"
    private const val KEY_LENGTH = 256
    
    suspend fun backupCriticalData(context: Context, data: Map<String, String>): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                if (!SecurityUtils.validateOperation("backup_data")) {
                    return@withContext false
                }
                
                val encryptedData = encryptData(data)
                val sanitizedFileName = "secure_backup_${System.currentTimeMillis()}.enc"
                if (!SecurityValidator.validateFilePath(sanitizedFileName)) {
                    throw SecurityException("Invalid backup file path")
                }
                val backupFile = File(context.filesDir, sanitizedFileName)
                
                backupFile.writeBytes(encryptedData)
                
                SecurityUtils.safeLog(
                    "SecureBackup",
                    "Critical data backed up successfully",
                    SecurityUtils.LogLevel.INFO
                )
                
                // Keep only last 5 backups
                cleanupOldBackups(context)
                
                true
            } catch (e: Exception) {
                SecurityUtils.safeLog(
                    "SecureBackup",
                    "Backup failed: ${e.message}",
                    SecurityUtils.LogLevel.ERROR
                )
                false
            }
        }
    }
    
    suspend fun restoreData(context: Context, backupFileName: String): Map<String, String>? {
        return withContext(Dispatchers.IO) {
            try {
                if (!SecurityUtils.validateOperation("restore_data")) {
                    return@withContext null
                }
                
                val backupFile = File(context.filesDir, backupFileName)
                if (!backupFile.exists()) {
                    SecurityUtils.safeLog(
                        "SecureBackup",
                        "Backup file not found: $backupFileName",
                        SecurityUtils.LogLevel.WARN
                    )
                    return@withContext null
                }
                
                val encryptedData = backupFile.readBytes()
                val decryptedData = decryptData(encryptedData)
                
                SecurityUtils.safeLog(
                    "SecureBackup",
                    "Data restored successfully from $backupFileName",
                    SecurityUtils.LogLevel.INFO
                )
                
                decryptedData
            } catch (e: Exception) {
                SecurityUtils.safeLog(
                    "SecureBackup",
                    "Restore failed: ${e.message}",
                    SecurityUtils.LogLevel.ERROR
                )
                null
            }
        }
    }
    
    private fun encryptData(data: Map<String, String>): ByteArray {
        return try {
            val key = generateSecretKey()
            val cipher = Cipher.getInstance(TRANSFORMATION)
            
            val secureRandom = SecureRandom.getInstanceStrong()
            val iv = ByteArray(16)
            secureRandom.nextBytes(iv)
            val ivSpec = IvParameterSpec(iv)
            
            cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec)
            
            val dataString = data.entries.joinToString("|") { "${it.key}:${it.value}" }
            val encryptedData = cipher.doFinal(dataString.toByteArray())
            
            // Combine IV + encrypted data
            iv + encryptedData
        } catch (e: Exception) {
            SecurityUtils.safeLog("SecureBackup", "Encryption failed", SecurityUtils.LogLevel.ERROR)
            throw SecurityException("Data encryption failed")
        }
    }
    
    private fun decryptData(encryptedBytes: ByteArray): Map<String, String> {
        return try {
            if (encryptedBytes.size < 16) {
                throw SecurityException("Invalid encrypted data")
            }
            
            val key = generateSecretKey()
            val cipher = Cipher.getInstance(TRANSFORMATION)
            
            // Extract IV (first 16 bytes)
            val iv = encryptedBytes.sliceArray(0..15)
            val encryptedData = encryptedBytes.sliceArray(16 until encryptedBytes.size)
            
            val ivSpec = IvParameterSpec(iv)
            cipher.init(Cipher.DECRYPT_MODE, key, ivSpec)
            
            val decryptedBytes = cipher.doFinal(encryptedData)
            val dataString = String(decryptedBytes)
            
            dataString.split("|").associate { entry ->
                val parts = entry.split(":", limit = 2)
                if (parts.size == 2) parts[0] to parts[1] else "" to ""
            }.filterKeys { it.isNotEmpty() }
        } catch (e: SecurityException) {
            throw e
        } catch (e: Exception) {
            SecurityUtils.safeLog("SecureBackup", "Decryption failed: ${e.message}", SecurityUtils.LogLevel.ERROR)
            throw SecurityException("Data decryption failed: ${e.javaClass.simpleName}")
        }
    }
    
    private fun generateSecretKey(): SecretKey {
        return try {
            // In production, this should be derived from user credentials or stored securely
            val keyBytes = "AfilaxySecureBackupKey2024!@#$".toByteArray().sliceArray(0..31)
            SecretKeySpec(keyBytes, ALGORITHM)
        } catch (e: Exception) {
            SecurityUtils.safeLog("SecureBackup", "Key generation failed: ${e.message}", SecurityUtils.LogLevel.ERROR)
            throw SecurityException("Failed to generate encryption key")
        }
    }
    
    private fun cleanupOldBackups(context: Context) {
        try {
            val backupFiles = context.filesDir.listFiles { _, name ->
                name.startsWith("secure_backup_") && name.endsWith(".enc")
            }?.sortedByDescending { it.lastModified() }
            
            backupFiles?.drop(5)?.forEach { file ->
                if (file.delete()) {
                    SecurityUtils.safeLog(
                        "SecureBackup",
                        "Old backup deleted: ${file.name}",
                        SecurityUtils.LogLevel.DEBUG
                    )
                }
            }
        } catch (e: SecurityException) {
            throw e
        } catch (e: Exception) {
            SecurityUtils.safeLog(
                "SecureBackup",
                "Cleanup failed: ${e.message}",
                SecurityUtils.LogLevel.ERROR
            )
        }
    }
    
    fun listAvailableBackups(context: Context): List<String> {
        return try {
            context.filesDir.listFiles { _, name ->
                name.startsWith("secure_backup_") && name.endsWith(".enc")
            }?.map { it.name }?.sorted() ?: emptyList()
        } catch (e: Exception) {
            SecurityUtils.safeLog(
                "SecureBackup",
                "Failed to list backups: ${e.message}",
                SecurityUtils.LogLevel.ERROR
            )
            emptyList()
        }
    }
}