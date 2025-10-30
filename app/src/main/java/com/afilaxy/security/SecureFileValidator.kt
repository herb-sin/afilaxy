package com.afilaxy.security

import java.io.File
import java.io.FileInputStream
import java.io.IOException

/**
 * Secure file validator to prevent CWE-434 (Unrestricted Upload of File with Dangerous Type)
 */
object SecureFileValidator {
    
    private val ALLOWED_EXTENSIONS = setOf(
        "jpg", "jpeg", "png", "gif", "webp", // Images
        "pdf", "txt", "doc", "docx", // Documents
        "mp3", "wav", "m4a" // Audio
    )
    
    private val ALLOWED_MIME_TYPES = setOf(
        "image/jpeg", "image/png", "image/gif", "image/webp",
        "application/pdf", "text/plain",
        "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "audio/mpeg", "audio/wav", "audio/mp4"
    )
    
    private val MAX_FILE_SIZE = 10 * 1024 * 1024 // 10MB
    
    fun validateFile(file: File): ValidationResult {
        return try {
            if (!AuthGuard.isUserAuthenticated()) {
                SecurityMonitor.logThreat("UNAUTHENTICATED_FILE_UPLOAD", file.name)
                return ValidationResult.Invalid(listOf("Authentication required"))
            }
            
            // Check file size
            if (file.length() > MAX_FILE_SIZE) {
                SecurityMonitor.logThreat("FILE_SIZE_EXCEEDED", "${file.name}: ${file.length()}")
                return ValidationResult.Invalid(listOf("File size exceeds limit"))
            }
            
            // Check extension
            val extension = file.extension.lowercase()
            if (extension !in ALLOWED_EXTENSIONS) {
                SecurityMonitor.logThreat("INVALID_FILE_EXTENSION", "${file.name}: $extension")
                return ValidationResult.Invalid(listOf("File type not allowed"))
            }
            
            // Check file content (magic bytes)
            if (!validateFileContent(file)) {
                SecurityMonitor.logThreat("INVALID_FILE_CONTENT", file.name)
                return ValidationResult.Invalid(listOf("File content validation failed"))
            }
            
            ValidationResult.Valid
        } catch (e: Exception) {
            SecureLogger.e("SecureFileValidator", "File validation error", e)
            ValidationResult.Invalid(listOf("Validation error"))
        }
    }
    
    private fun validateFileContent(file: File): Boolean {
        return try {
            FileInputStream(file).use { fis ->
                val header = ByteArray(8)
                val bytesRead = fis.read(header)
                
                if (bytesRead < 4) return false
                
                when (file.extension.lowercase()) {
                    "jpg", "jpeg" -> isJpegFile(header)
                    "png" -> isPngFile(header)
                    "gif" -> isGifFile(header)
                    "pdf" -> isPdfFile(header)
                    else -> true // Allow other validated extensions
                }
            }
        } catch (e: IOException) {
            SecureLogger.e("SecureFileValidator", "Content validation error", e)
            false
        }
    }
    
    private fun isJpegFile(header: ByteArray): Boolean {
        return header.size >= 3 && 
               header[0] == 0xFF.toByte() && 
               header[1] == 0xD8.toByte() && 
               header[2] == 0xFF.toByte()
    }
    
    private fun isPngFile(header: ByteArray): Boolean {
        return header.size >= 8 &&
               header[0] == 0x89.toByte() &&
               header[1] == 0x50.toByte() &&
               header[2] == 0x4E.toByte() &&
               header[3] == 0x47.toByte()
    }
    
    private fun isGifFile(header: ByteArray): Boolean {
        return header.size >= 6 &&
               header[0] == 0x47.toByte() &&
               header[1] == 0x49.toByte() &&
               header[2] == 0x46.toByte()
    }
    
    private fun isPdfFile(header: ByteArray): Boolean {
        return header.size >= 4 &&
               header[0] == 0x25.toByte() &&
               header[1] == 0x50.toByte() &&
               header[2] == 0x44.toByte() &&
               header[3] == 0x46.toByte()
    }
}