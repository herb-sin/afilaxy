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
            // Authentication check for input validation
            if (!com.afilaxy.security.AuthGuard.isUserAuthenticated()) {
                SecureLogger.security("INPUT_VALIDATION", "UNAUTHENTICATED_ACCESS")
                return false
            }
            !containsSqlInjection(input) && !containsPathTraversal(input)
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Input validation error", e)
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
    
    private val SAFE_EXTENSIONS = setOf(".jpg", ".jpeg", ".png", ".pdf", ".txt")
    
    private val DANGEROUS_EXTENSIONS = setOf(
        ".exe", ".bat", ".cmd", ".com", ".pif", ".scr", ".msi", ".app", ".run", ".bin",
        ".vbs", ".js", ".ps1", ".py", ".rb", ".pl", ".lua", ".sh", ".php", ".asp", ".jsp",
        ".zip", ".rar", ".7z", ".tar", ".gz", ".bz2", ".xz", ".iso", ".img", ".cab",
        ".dll", ".so", ".dylib", ".jar", ".class", ".war", ".ear", ".dex",
        ".reg", ".inf", ".cpl", ".msc", ".lnk", ".url", ".gadget", ".application",
        ".html", ".htm", ".svg", ".xml", ".xhtml", ".xht", ".xsl", ".xslt",
        ".deb", ".pkg", ".dmg", ".apk", ".ipa", ".rpm",
        ".hta", ".wsf", ".wsh", ".vbe", ".jse", ".wsc", ".msp", ".mst"
    )
    
    fun validateFileExtension(filename: String): Boolean {
        return SecurityInterceptor.secureOperation("file_extension_validation") {
            if (filename.isBlank() || filename.length > 255 || !filename.contains('.')) {
                SecurityMonitor.reportSecurityEvent("FILE_UPLOAD_VIOLATION", "Invalid filename format")
                return@secureOperation false
            }
            
            val normalizedName = filename.lowercase().trim()
            
            // Strict control character and null byte detection
            if (normalizedName.any { it.isISOControl() || it.code < 32 || it.code == 127 || it == '\u0000' }) {
                SecurityMonitor.reportSecurityEvent("FILE_UPLOAD_VIOLATION", "Control characters detected")
                return@secureOperation false
            }
            
            // Zero-tolerance suspicious pattern detection
            val suspiciousPatterns = listOf(
                "con.", "prn.", "aux.", "nul.", "com1.", "com2.", "lpt1.", "lpt2.",
                "..", "./", ".\\\\", "~", "%", "$", "&", "|", ";", "<", ">", "?", "*", ":"
            )
            if (suspiciousPatterns.any { normalizedName.contains(it, ignoreCase = true) }) {
                SecurityMonitor.reportSecurityEvent("FILE_UPLOAD_VIOLATION", "Suspicious pattern: $filename")
                return@secureOperation false
            }
            
            val parts = normalizedName.split('.')
            if (parts.size < 2 || parts.size > 3) {
                SecurityMonitor.reportSecurityEvent("FILE_UPLOAD_VIOLATION", "Invalid extension count")
                return@secureOperation false
            }
            
            val allExtensions = parts.drop(1).map { ".$it" }
            
            // ABSOLUTE rejection of ANY dangerous extension
            if (allExtensions.any { ext -> DANGEROUS_EXTENSIONS.contains(ext) }) {
                SecurityMonitor.reportSecurityEvent("FILE_UPLOAD_VIOLATION", "Dangerous extension: $filename")
                return@secureOperation false
            }
            
            // Enhanced malware indicator detection
            val malwareIndicators = listOf(
                "setup", "install", "update", "patch", "crack", "keygen", "loader", "hack",
                "exploit", "payload", "shell", "backdoor", "trojan", "virus", "malware", "worm"
            )
            if (malwareIndicators.any { normalizedName.contains(it, ignoreCase = true) }) {
                SecurityMonitor.reportSecurityEvent("FILE_UPLOAD_VIOLATION", "Malware indicator: $filename")
                return@secureOperation false
            }
            
            // STRICT WHITELIST - Only medical app safe extensions
            val finalExtension = allExtensions.last()
            if (!SAFE_EXTENSIONS.contains(finalExtension)) {
                SecurityMonitor.reportSecurityEvent("FILE_UPLOAD_VIOLATION", "Non-whitelisted extension: $finalExtension")
                return@secureOperation false
            }
            
            // Ultra-strict base filename validation
            val baseFilename = parts.first()
            val isValidBase = baseFilename.length in 1..50 && 
                             baseFilename.matches(Regex("^[a-zA-Z0-9_-]+$")) &&
                             !baseFilename.startsWith("_") &&
                             !baseFilename.endsWith("_")
            
            if (!isValidBase) {
                SecurityMonitor.reportSecurityEvent("FILE_UPLOAD_VIOLATION", "Invalid base filename: $baseFilename")
                return@secureOperation false
            }
            
            true
        } ?: false
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
    
    /**
     * Validate geographic coordinates
     */
    fun validateCoordinates(latitude: Double, longitude: Double): Boolean {
        return try {
            if (!AuthGuard.requireAuthentication("coordinate_validation")) {
                return false
            }
            
            val isValidLat = latitude in -90.0..90.0 && !latitude.isNaN() && !latitude.isInfinite()
            val isValidLon = longitude in -180.0..180.0 && !longitude.isNaN() && !longitude.isInfinite()
            
            isValidLat && isValidLon
        } catch (e: Exception) {
            SecurityUtils.safeLog(TAG, "Coordinate validation error", SecurityUtils.LogLevel.ERROR)
            false
        }
    }
}