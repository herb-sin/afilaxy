package com.afilaxy.security

import android.util.Log
import java.io.File
import java.util.regex.Pattern
import javax.xml.parsers.DocumentBuilderFactory

object SecurityValidator {
    
    private const val TAG = "SecurityValidator"
    
    // SQL Injection Prevention
    private val SQL_INJECTION_PATTERNS = listOf(
        Pattern.compile("('|(\\-\\-)|(;)|(\\|)|(\\*)|(%))", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(union|select|insert|update|delete|drop|create|alter)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(script|javascript|vbscript|onload|onerror)", Pattern.CASE_INSENSITIVE)
    )
    
    // Path Traversal Prevention
    private val PATH_TRAVERSAL_PATTERNS = listOf(
        Pattern.compile("(\\.\\./|\\.\\.\\\\)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(\\\\|/)(etc|proc|sys|dev|root)", Pattern.CASE_INSENSITIVE)
    )
    
    fun validateInput(input: String): Boolean {
        return try {
            !containsSqlInjection(input) && !containsPathTraversal(input)
        } catch (e: Exception) {
            Log.e(TAG, "Input validation error", e)
            false
        }
    }
    
    fun sanitizeInput(input: String): String {
        return input
            .replace("'", "''")
            .replace("--", "")
            .replace(";", "")
            .replace("|", "")
            .replace("*", "")
            .replace("%", "")
            .replace("../", "")
            .replace("..\\", "")
    }
    
    private fun containsSqlInjection(input: String): Boolean {
        return SQL_INJECTION_PATTERNS.any { it.matcher(input).find() }
    }
    
    private fun containsPathTraversal(input: String): Boolean {
        return PATH_TRAVERSAL_PATTERNS.any { it.matcher(input).find() }
    }
    
    // XXE Prevention for XML parsing
    fun createSecureDocumentBuilder(): DocumentBuilderFactory? {
        return try {
            DocumentBuilderFactory.newInstance().apply {
                setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
                setFeature("http://xml.org/sax/features/external-general-entities", false)
                setFeature("http://xml.org/sax/features/external-parameter-entities", false)
                setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
                isXIncludeAware = false
                isExpandEntityReferences = false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create secure document builder", e)
            null
        }
    }
    
    // Safe file extensions - medical app specific (whitelist approach)
    private val SAFE_EXTENSIONS = setOf(".jpg", ".jpeg", ".png", ".pdf", ".txt")
    
    // Comprehensive dangerous extensions blocklist
    private val DANGEROUS_EXTENSIONS = setOf(
        ".exe", ".bat", ".cmd", ".com", ".pif", ".scr", ".vbs", ".js", ".jar",
        ".app", ".deb", ".pkg", ".dmg", ".sh", ".php", ".asp", ".jsp", ".html", ".htm",
        ".apk", ".ipa", ".msi", ".dll", ".so", ".dylib", ".bin", ".run", ".zip", ".rar",
        ".7z", ".tar", ".gz", ".bz2", ".xz", ".iso", ".img", ".svg", ".xml"
    )
    
    fun validateFileExtension(filename: String): Boolean {
        return try {
            if (filename.isBlank() || !filename.contains('.') || filename.length > 255) return false
            
            val normalizedName = filename.lowercase().trim()
            
            // Prevent null byte injection and control characters
            if (normalizedName.contains('\u0000') || normalizedName.any { it.isISOControl() }) {
                return false
            }
            
            // Check for double extensions (e.g., file.jpg.exe)
            val allExtensions = normalizedName.split('.').drop(1).map { ".$it" }
            
            // Block if any extension is dangerous
            if (allExtensions.any { DANGEROUS_EXTENSIONS.contains(it) }) return false
            
            // Only allow if final extension is safe (whitelist approach)
            val finalExtension = allExtensions.lastOrNull() ?: return false
            SAFE_EXTENSIONS.contains(finalExtension)
        } catch (e: Exception) {
            Log.e(TAG, "File extension validation error", e)
            false
        }
    }
    
    fun validateFileName(filename: String): Boolean {
        // Check for null bytes and control characters that could bypass validation
        if (filename.contains('\u0000') || filename.any { it.isISOControl() }) {
            return false
        }
        
        return filename.length in 1..255 &&
               !filename.contains("..") &&
               !filename.contains("/") &&
               !filename.contains("\\") &&
               !filename.contains(":") &&
               !filename.contains("*") &&
               !filename.contains("?") &&
               !filename.contains('"') &&
               !filename.contains("<") &&
               !filename.contains(">") &&
               !filename.contains("|") &&
               filename.matches(Regex("^[a-zA-Z0-9._-]+$")) &&
               validateFileExtension(filename)
    }
    
    // File path validation with strict security
    fun validateFilePath(path: String): Boolean {
        return try {
            // Reject paths with dangerous patterns immediately
            if (containsPathTraversal(path) || path.contains("null") || path.length > 500) {
                return false
            }
            
            // Early extension validation to prevent dangerous file processing
            val filename = path.substringAfterLast('/', path.substringAfterLast('\\'))
            if (filename.isBlank() || filename.length > 255 || !filename.contains('.') || !validateFileExtension(filename)) {
                return false
            }
            
            val file = File(path).canonicalFile
            val allowedDirs = listOf(
                File("/data/data/com.afilaxy/files").canonicalFile,
                File("/data/data/com.afilaxy/cache").canonicalFile
            )
            
            // Must be within allowed directories and have safe extension
            allowedDirs.any { allowedDir -> 
                file.path.startsWith(allowedDir.path)
            } && validateFileName(file.name)
            
        } catch (e: Exception) {
            Log.e(TAG, "Path validation error: ${e.javaClass.simpleName}", e)
            false
        }
    }
    
    // Validate file content type (MIME type validation)
    fun validateFileContent(file: File): Boolean {
        return try {
            if (!file.exists() || file.length() > 10_000_000) return false // 10MB limit
            
            val bytes = file.inputStream().use { it.readNBytes(8) }
            when {
                // JPEG magic bytes
                bytes.size >= 3 && bytes[0] == 0xFF.toByte() && 
                bytes[1] == 0xD8.toByte() && bytes[2] == 0xFF.toByte() -> true
                
                // PNG magic bytes
                bytes.size >= 8 && bytes[0] == 0x89.toByte() && 
                bytes[1] == 0x50.toByte() && bytes[2] == 0x4E.toByte() && 
                bytes[3] == 0x47.toByte() -> true
                
                // PDF magic bytes
                bytes.size >= 4 && bytes[0] == 0x25.toByte() && 
                bytes[1] == 0x50.toByte() && bytes[2] == 0x44.toByte() && 
                bytes[3] == 0x46.toByte() -> true
                
                else -> false
            }
        } catch (e: Exception) {
            Log.e(TAG, "File content validation error", e)
            false
        }
    }
}