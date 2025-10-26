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
            } catch (e: SecurityException) {
                SecureLogger.security("BACKUP_OPERATION", "SECURITY_VIOLATION")
                false
            } catch (e: Exception) {
                SecureLogger.e("SecureBackup", "Backup failed", e)
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
            } catch (e: SecurityException) {
                SecureLogger.security("RESTORE_OPERATION", "SECURITY_VIOLATION")
                null
            } catch (e: Exception) {
                SecureLogger.e("SecureBackup", "Restore failed", e)
                null
            }
        }
    }
    
    private fun encryptData(data: Map<String, String>): ByteArray {
        return try {
            val key = generateSecretKey()
            val cipher = Cipher.getInstance(TRANSFORMATION)
            
            // Generate cryptographically secure random IV with entropy validation
            val iv = generateSecureIV()
            val ivSpec = IvParameterSpec(iv)
            
            cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec)
            
            val dataString = data.entries.joinToString("|") { "${it.key}:${it.value}" }
            val encryptedData = cipher.doFinal(dataString.toByteArray())
            
            // Combine IV + encrypted data
            iv + encryptedData
        } catch (e: SecurityException) {
            throw e
        } catch (e: Exception) {
            SecureLogger.e("SecureBackup", "Encryption failed", e)
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
            
            // Extract IV (first 16 bytes) with validation
            val iv = encryptedBytes.sliceArray(0..15)
            val encryptedData = encryptedBytes.sliceArray(16 until encryptedBytes.size)
            
            // Validate IV entropy and unpredictability
            if (!isValidIV(iv)) {
                throw SecurityException("Invalid or predictable IV detected")
            }
            
            val ivSpec = IvParameterSpec(iv)
            cipher.init(Cipher.DECRYPT_MODE, key, ivSpec)
            
            val decryptedBytes = cipher.doFinal(encryptedData)
            val dataString = String(decryptedBytes)
            
            dataString.split("|").associate { entry ->
                val parts = entry.split(":", limit = 2)
                if (parts.size == 2) parts[0] to parts[1] else "" to ""
            }.filterKeys { it.isNotEmpty() }
        } catch (e: SecurityException) {
            SecurityUtils.safeLog("SecureBackup", "Security violation in decryption", SecurityUtils.LogLevel.SECURITY)
            throw e
        } catch (e: Exception) {
            SecurityUtils.safeLog("SecureBackup", "Decryption failed", SecurityUtils.LogLevel.ERROR)
            throw SecurityException("Data decryption failed")
        }
    }
    
    private fun generateSecretKey(): SecretKey {
        return try {
            // In production, this should be derived from user credentials or stored securely
            val keyBytes = "AfilaxySecureBackupKey2024!@#$".toByteArray().sliceArray(0..31)
            SecretKeySpec(keyBytes, ALGORITHM)
        } catch (e: Exception) {
            SecureLogger.e("SecureBackup", "Key generation failed", e)
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
            SecureLogger.e("SecureBackup", "Cleanup failed", e)
            // Don't rethrow to prevent cascade failures
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
    
    private fun generateSecureIV(): ByteArray {
        try {
            val secureRandom = SecureRandom.getInstanceStrong()
            
            // Multiple attempts with enhanced entropy validation
            repeat(20) { attempt ->
                val iv = ByteArray(16)
                secureRandom.nextBytes(iv)
                
                // Add multiple entropy sources
                val nanoTime = System.nanoTime()
                val currentTime = System.currentTimeMillis()
                val memoryHash = System.identityHashCode(this)
                val threadId = Thread.currentThread().id
                
                val combinedEntropy = (nanoTime xor currentTime xor memoryHash.toLong() xor threadId).toInt()
                
                // Mix entropy into IV with secure distribution
                for (i in iv.indices step 4) {
                    if (i + 3 < iv.size) {
                        val entropyBytes = (combinedEntropy xor (attempt * 31)).toByteArray()
                        for (j in 0..3) {
                            if (i + j < iv.size && j < entropyBytes.size) {
                                iv[i + j] = (iv[i + j].toInt() xor entropyBytes[j].toInt()).toByte()
                            }
                        }
                    }
                }
                
                // Enhanced validation with ultra-strict entropy requirements
                if (isValidIV(iv) && hasHighEntropy(iv) && passesEntropyTests(iv) && passesAdvancedRandomnessTests(iv)) {
                    return iv
                }
            }
            
            throw SecurityException("Failed to generate cryptographically secure IV after 20 attempts")
        } catch (e: SecurityException) {
            throw e
        } catch (e: Exception) {
            SecureLogger.e("SecureBackup", "IV generation failed", e)
            throw SecurityException("IV generation error: ${e.message}")
        }
    }
    
    private fun isValidIV(iv: ByteArray): Boolean {
        if (iv.size != 16) return false
        
        val predictablePatterns = listOf(
            ByteArray(16) { 0 },
            ByteArray(16) { 1 },
            ByteArray(16) { 0xFF.toByte() },
            ByteArray(16) { it.toByte() },
            ByteArray(16) { (it % 2).toByte() }
        )
        
        if (predictablePatterns.any { iv.contentEquals(it) }) {
            return false
        }
        
        val uniqueBytes = iv.toSet().size
        if (uniqueBytes < 8) return false
        
        for (i in 0 until iv.size - 4) {
            var consecutiveCount = 1
            for (j in i + 1 until minOf(i + 5, iv.size)) {
                if (iv[i] == iv[j]) {
                    consecutiveCount++
                    if (consecutiveCount > 4) return false
                }
            }
        }
        
        return true
    }
    
    private fun hasHighEntropy(iv: ByteArray): Boolean {
        try {
            val expected = iv.size / 256.0
            val counts = IntArray(256)
            
            iv.forEach { byte ->
                counts[byte.toUByte().toInt()]++
            }
            
            var chiSquare = 0.0
            counts.forEach { count ->
                val diff = count - expected
                chiSquare += (diff * diff) / expected
            }
            
            // Stricter chi-square test for better entropy validation
            return chiSquare < 200.0 && chiSquare > 50.0
        } catch (e: Exception) {
            SecureLogger.e("SecureBackup", "Entropy validation failed", e)
            return false
        }
    }
    
    private fun passesEntropyTests(iv: ByteArray): Boolean {
        try {
            // Test 1: Hamming weight (number of 1 bits should be roughly half)
            val totalBits = iv.size * 8
            val oneBits = iv.sumOf { byte -> byte.toUByte().countOneBits() }
            val hammingRatio = oneBits.toDouble() / totalBits
            if (hammingRatio < 0.4 || hammingRatio > 0.6) return false
            
            // Test 2: Run test (no long sequences of same bit)
            val bitString = iv.joinToString("") { byte ->
                byte.toUByte().toString(2).padStart(8, '0')
            }
            var maxRun = 1
            var currentRun = 1
            for (i in 1 until bitString.length) {
                if (bitString[i] == bitString[i-1]) {
                    currentRun++
                    maxRun = maxOf(maxRun, currentRun)
                } else {
                    currentRun = 1
                }
            }
            if (maxRun > 8) return false
            
            // Test 3: Autocorrelation test
            var autocorr = 0
            for (i in 0 until iv.size - 1) {
                autocorr += (iv[i].toInt() xor iv[i + 1].toInt()).countOneBits()
            }
            val autocorrRatio = autocorr.toDouble() / ((iv.size - 1) * 8)
            if (autocorrRatio < 0.4 || autocorrRatio > 0.6) return false
            
            return true
        } catch (e: Exception) {
            SecureLogger.e("SecureBackup", "Entropy test failed", e)
            return false
        }
    }
    
    private fun passesAdvancedRandomnessTests(iv: ByteArray): Boolean {
        try {
            // Test 1: Monobit frequency test
            val oneBits = iv.sumOf { it.toUByte().countOneBits() }
            val totalBits = iv.size * 8
            val frequency = kotlin.math.abs(oneBits - totalBits / 2.0)
            if (frequency > totalBits * 0.1) return false
            
            // Test 2: Block frequency test
            val blockSize = 8
            val blocks = iv.size / blockSize
            var blockFreqSum = 0.0
            for (i in 0 until blocks) {
                val blockStart = i * blockSize
                val blockEnd = minOf(blockStart + blockSize, iv.size)
                val blockOneBits = iv.sliceArray(blockStart until blockEnd)
                    .sumOf { it.toUByte().countOneBits() }
                val blockFreq = blockOneBits.toDouble() / (blockSize * 8)
                blockFreqSum += (blockFreq - 0.5) * (blockFreq - 0.5)
            }
            if (blockFreqSum > 0.1) return false
            
            // Test 3: Poker test (check for uniform distribution of 4-bit patterns)
            val patterns = IntArray(16)
            for (byte in iv) {
                val high = (byte.toUByte().toInt() shr 4) and 0x0F
                val low = byte.toUByte().toInt() and 0x0F
                patterns[high]++
                patterns[low]++
            }
            val expected = iv.size * 2.0 / 16
            val chiSquare = patterns.sumOf { count ->
                val diff = count - expected
                (diff * diff) / expected
            }
            if (chiSquare > 25.0 || chiSquare < 5.0) return false
            
            return true
        } catch (e: Exception) {
            SecureLogger.e("SecureBackup", "Advanced randomness test failed", e)
            return false
        }
    }
    
    private fun Int.toByteArray(): ByteArray {
        return byteArrayOf(
            (this shr 24).toByte(),
            (this shr 16).toByte(), 
            (this shr 8).toByte(),
            this.toByte()
        )
    }
}