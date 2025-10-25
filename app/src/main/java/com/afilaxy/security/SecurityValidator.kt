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
    
    // Comprehensive dangerous extensions blocklist - CRITICAL SECURITY
    private val DANGEROUS_EXTENSIONS = setOf(
        // Executables
        ".exe", ".bat", ".cmd", ".com", ".pif", ".scr", ".msi", ".app", ".run",
        // Scripts
        ".vbs", ".js", ".ps1", ".py", ".rb", ".pl", ".lua", ".sh", ".php", ".asp", ".jsp",
        // Archives (potential malware containers)
        ".zip", ".rar", ".7z", ".tar", ".gz", ".bz2", ".xz", ".iso", ".img",
        // Libraries/Binaries
        ".dll", ".so", ".dylib", ".jar", ".class", ".war", ".ear",
        // System files
        ".reg", ".inf", ".cpl", ".msc", ".lnk", ".url", ".gadget", ".application",
        // Web/Markup (XSS risk)
        ".html", ".htm", ".svg", ".xml", ".xhtml", ".xht",
        // Package files
        ".deb", ".pkg", ".dmg", ".apk", ".ipa",
        // Windows specific
        ".hta", ".wsf", ".wsh", ".vbe", ".jse", ".wsc", ".wsf"
    )
    
    fun validateFileExtension(filename: String): Boolean {
        return try {
            if (filename.isBlank() || !filename.contains('.') || filename.length > 255) return false
            
            val normalizedName = filename.lowercase().trim()
            
            // CRITICAL: Prevent null byte injection and control characters
            if (normalizedName.contains('\u0000') || normalizedName.any { it.isISOControl() }) {
                SecureLogger.w(TAG, "Control characters detected in filename")
                return false
            }
            
            // CRITICAL: Extract all extensions to prevent double extension attacks
            val parts = normalizedName.split('.')
            if (parts.size < 2) return false // Must have extension
            
            val allExtensions = parts.drop(1).map { ".$it" }
            
            // CRITICAL: Block ANY dangerous extension in the chain
            val hasDangerousExtension = allExtensions.any { ext -> 
                DANGEROUS_EXTENSIONS.contains(ext)
            }
            
            if (hasDangerousExtension) {
                SecureLogger.security("FILE_VALIDATION", "DANGEROUS_EXTENSION_BLOCKED")
                return false
            }
            
            // CRITICAL: Additional check for executable patterns
            val hasExecutablePattern = normalizedName.contains(".exe.") || 
                                     normalizedName.contains(".bat.") ||
                                     normalizedName.contains(".scr.")
            
            if (hasExecutablePattern) {
                SecureLogger.security("FILE_VALIDATION", "EXECUTABLE_PATTERN_BLOCKED")
                return false
            }
            
            // WHITELIST ONLY: Final extension must be explicitly safe
            val finalExtension = allExtensions.lastOrNull() ?: return false
            val isAllowed = SAFE_EXTENSIONS.contains(finalExtension)
            
            if (!isAllowed) {
                SecureLogger.w(TAG, "File extension not in whitelist")
            }
            
            isAllowed
        } catch (e: Exception) {
            SecureLogger.e(TAG, "File extension validation error", e)
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
            Log.e(TAG, "Path validation error", e)
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