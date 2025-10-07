package com.afilaxy.security

import java.util.regex.Pattern

object InputSanitizer {
    
    // Strict whitelist patterns for security
    private val EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")
    private val NAME_PATTERN = Pattern.compile("^[a-zA-ZÀ-ÿ\\s'-]{1,100}$")
    private val PHONE_PATTERN = Pattern.compile("^[0-9()\\s+-]{10,15}$")
    private val ALPHANUMERIC_PATTERN = Pattern.compile("^[a-zA-Z0-9\\s]{1,500}$")
    
    // Firestore-safe character mapping (prevents NoSQL injection)
    private val FIRESTORE_SAFE_CHARS = mapOf(
        "." to "_dot_",
        "#" to "_hash_",
        "$" to "_dollar_",
        "/" to "_slash_",
        "[" to "_lbracket_",
        "]" to "_rbracket_",
        "'" to "_quote_",
        "\"" to "_dquote_",
        "\\" to "_backslash_"
    )
    
    fun sanitizeEmail(email: String?): String {
        if (email.isNullOrBlank()) return ""
        val cleaned = email.trim().lowercase()
        return if (EMAIL_PATTERN.matcher(cleaned).matches()) cleaned else ""
    }
    
    fun sanitizeName(name: String?): String {
        if (name.isNullOrBlank()) return ""
        val cleaned = name.trim()
        return if (NAME_PATTERN.matcher(cleaned).matches()) cleaned else ""
    }
    
    fun sanitizePhone(phone: String?): String {
        if (phone.isNullOrBlank()) return ""
        val cleaned = phone.replace("[^0-9()\\s+-]".toRegex(), "")
        return if (PHONE_PATTERN.matcher(cleaned).matches()) cleaned else ""
    }
    
    fun sanitizeForFirestore(input: String?): String {
        if (input.isNullOrBlank()) return ""
        var sanitized = input.trim().take(500)
        
        // Replace dangerous characters
        FIRESTORE_SAFE_CHARS.forEach { (char, replacement) ->
            sanitized = sanitized.replace(char, replacement)
        }
        
        // Remove any remaining special characters that could cause injection
        sanitized = sanitized.replace("[{}\\[\\]()$]".toRegex(), "")
        
        return sanitized
    }
    
    fun sanitizeText(text: String?): String {
        if (text.isNullOrBlank()) return ""
        val cleaned = text.trim()
        return if (ALPHANUMERIC_PATTERN.matcher(cleaned).matches()) {
            cleaned.take(1000)
        } else {
            // Fallback: remove dangerous characters
            cleaned.replace("[<>\"'&{}$\\[\\]()]".toRegex(), "").take(1000)
        }
    }
    
    fun isValidEmail(email: String?): Boolean {
        return !email.isNullOrBlank() && EMAIL_PATTERN.matcher(email.trim().lowercase()).matches()
    }
    
    fun isValidName(name: String?): Boolean {
        return !name.isNullOrBlank() && NAME_PATTERN.matcher(name.trim()).matches()
    }
    
    // Prevent NoSQL injection in queries
    fun sanitizeQueryParam(param: String?): String {
        if (param.isNullOrBlank()) return ""
        return param.trim()
            .replace("[\${}\\[\\]()'\";]".toRegex(), "")
            .take(100)
    }
}