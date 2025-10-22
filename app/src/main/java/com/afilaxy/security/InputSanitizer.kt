package com.afilaxy.security

import java.util.regex.Pattern

object InputSanitizer {
    
    // Strict whitelist patterns - NoSQL injection prevention
    private val EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$")
    private val NAME_PATTERN = Pattern.compile("^[a-zA-ZÀ-ÿ\\s]{1,50}$")
    private val PHONE_PATTERN = Pattern.compile("^[0-9()\\s+-]{10,15}$")
    private val SAFE_TEXT_PATTERN = Pattern.compile("^[a-zA-Z0-9\\s._-]{1,200}$")
    
    // Enhanced NoSQL operator detection
    private val NOSQL_OPERATORS = setOf(
        "\$where", "\$ne", "\$gt", "\$lt", "\$gte", "\$lte", "\$in", "\$nin", 
        "\$regex", "\$or", "\$and", "\$not", "\$exists", "\$elemMatch", "\$size",
        "\$all", "\$mod", "\$type", "\$slice", "\$push", "\$pull", "\$set", "\$unset"
    )
    
    // Enhanced NoSQL injection detection with stricter patterns
    private val NOSQL_INJECTION_PATTERN = Pattern.compile(
        "(\\\$[a-zA-Z_]+|javascript:|eval\\(|function\\(|setTimeout|setInterval|\\{[^}]*\\}|\\[[^\\]]*\\]|ObjectId\\(|new\\s+\\w+)",
        Pattern.CASE_INSENSITIVE
    )
    
    // Comprehensive NoSQL injection prevention - enhanced
    private val BLOCKED_PATTERNS = setOf(
        "\\\$where", "\\\$ne", "\\\$gt", "\\\$lt", "\\\$gte", "\\\$lte", "\\\$in", "\\\$nin", 
        "\\\$regex", "\\\$or", "\\\$and", "\\\$not", "\\\$exists", "\\\$elemMatch", "\\\$size",
        "\\\$all", "\\\$mod", "\\\$type", "\\\$slice", "\\\$push", "\\\$pull", "\\\$set", "\\\$unset",
        "javascript:", "eval\\(", "function\\(", "setTimeout\\(", "setInterval\\(", "constructor",
        "prototype", "__proto__", "toString", "valueOf"
    )
    private val DANGEROUS_CHARS = "\${}[]();'\"\\/*<>=".toCharArray().toSet()
    
    // Firestore-safe character mapping (prevents NoSQL injection) - enhanced
    private val FIRESTORE_SAFE_CHARS = mapOf(
        "." to "_dot_",
        "#" to "_hash_",
        "$" to "_dollar_",
        "/" to "_slash_",
        "[" to "_lbracket_",
        "]" to "_rbracket_",
        "'" to "_quote_",
        "\"" to "_dquote_",
        "\\" to "_backslash_",
        "{" to "_lbrace_",
        "}" to "_rbrace_",
        "(" to "_lparen_",
        ")" to "_rparen_"
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
        
        return try {
            var sanitized = input.trim().take(500)
            
            // Check for NoSQL injection patterns first
            if (containsNoSQLInjection(sanitized)) {
                android.util.Log.w("InputSanitizer", "NoSQL injection attempt blocked")
                return ""
            }
            
            // Replace dangerous characters
            FIRESTORE_SAFE_CHARS.forEach { (char, replacement) ->
                sanitized = sanitized.replace(char, replacement)
            }
            
            // Remove any remaining special characters that could cause injection
            sanitized = sanitized.replace("[{}\\[\\]()$]".toRegex(), "")
            
            sanitized
        } catch (e: Exception) {
            android.util.Log.e("InputSanitizer", "Firestore sanitization error", e)
            ""
        }
    }
    
    fun sanitizeText(text: String?): String {
        if (text.isNullOrBlank()) return ""
        var cleaned = text.trim().take(200)
        
        // Early detection of NoSQL injection patterns
        if (NOSQL_INJECTION_PATTERN.matcher(cleaned).find()) {
            android.util.Log.w("InputSanitizer", "Potential NoSQL injection detected")
            return ""
        }
        
        // Remove NoSQL injection patterns
        BLOCKED_PATTERNS.forEach { pattern ->
            cleaned = cleaned.replace(pattern, "", ignoreCase = true)
        }
        
        // Remove dangerous characters
        DANGEROUS_CHARS.forEach { char ->
            cleaned = cleaned.replace(char.toString(), "")
        }
        
        return if (SAFE_TEXT_PATTERN.matcher(cleaned).matches()) cleaned else ""
    }
    
    fun isValidEmail(email: String?): Boolean {
        return !email.isNullOrBlank() && EMAIL_PATTERN.matcher(email.trim().lowercase()).matches()
    }
    
    fun isValidName(name: String?): Boolean {
        return !name.isNullOrBlank() && NAME_PATTERN.matcher(name.trim()).matches()
    }
    
    // Prevent NoSQL injection in queries - enhanced security
    fun sanitizeQueryParam(param: String?): String {
        if (param.isNullOrBlank()) return ""
        
        return try {
            var sanitized = param.trim().take(100)
            
            // Remove NoSQL operators
            BLOCKED_PATTERNS.forEach { operator ->
                sanitized = sanitized.replace(operator, "", ignoreCase = true)
            }
            
            // Remove dangerous characters
            DANGEROUS_CHARS.forEach { char ->
                sanitized = sanitized.replace(char.toString(), "")
            }
            
            sanitized
        } catch (e: Exception) {
            android.util.Log.e("InputSanitizer", "Error sanitizing query param", e)
            ""
        }
    }
    
    fun preventNoSQLInjection(input: String?): String {
        if (input.isNullOrBlank()) return ""
        
        return try {
            // Use existing sanitizeText which already handles NoSQL injection
            sanitizeText(input)
        } catch (e: Exception) {
            android.util.Log.e("InputSanitizer", "NoSQL injection prevention failed", e)
            ""
        }
    }
    
    // Validate coordinates to prevent injection
    fun sanitizeCoordinates(lat: Double?, lon: Double?): Pair<Double, Double>? {
        return try {
            if (lat == null || lon == null) return null
            if (!SecurityUtils.isValidCoordinate(lat, lon)) return null
            Pair(lat, lon)
        } catch (e: Exception) {
            android.util.Log.e("InputSanitizer", "Coordinate validation failed", e)
            null
        }
    }
    
    // Helper function to detect NoSQL injection patterns
    private fun containsNoSQLInjection(input: String): Boolean {
        return try {
            // Check for NoSQL operators
            NOSQL_OPERATORS.any { operator -> 
                input.contains(operator, ignoreCase = true) 
            } || NOSQL_INJECTION_PATTERN.matcher(input).find()
        } catch (e: Exception) {
            android.util.Log.e("InputSanitizer", "NoSQL detection error", e)
            true // Fail safe - assume injection if error occurs
        }
    }
    
    // Validate coordinates safely
    private fun isValidCoordinate(lat: Double, lon: Double): Boolean {
        return lat in -90.0..90.0 && lon in -180.0..180.0
    }
}