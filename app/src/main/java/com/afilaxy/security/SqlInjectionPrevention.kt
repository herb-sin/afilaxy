package com.afilaxy.security

import java.util.regex.Pattern

/**
 * SQL Injection prevention utilities for Afilaxy
 * Provides comprehensive protection against SQL injection attacks
 */
object SqlInjectionPrevention {
    
    // Comprehensive SQL injection patterns
    private val SQL_INJECTION_PATTERNS = listOf(
        // Basic SQL injection patterns
        Pattern.compile("('|(\\-\\-)|(;)|(\\|)|(\\*)|(%))", Pattern.CASE_INSENSITIVE),
        
        // SQL keywords
        Pattern.compile("\\b(union|select|insert|update|delete|drop|create|alter|exec|execute)\\b", Pattern.CASE_INSENSITIVE),
        
        // SQL functions
        Pattern.compile("\\b(concat|substring|ascii|char|nchar|db_name|user_name|system_user)\\b", Pattern.CASE_INSENSITIVE),
        
        // SQL operators
        Pattern.compile("(\\bor\\b|\\band\\b)\\s+\\d+\\s*=\\s*\\d+", Pattern.CASE_INSENSITIVE),
        
        // Comment patterns
        Pattern.compile("(/\\*|\\*/|--)", Pattern.CASE_INSENSITIVE),
        
        // Hex encoding
        Pattern.compile("0x[0-9a-f]+", Pattern.CASE_INSENSITIVE),
        
        // Script injection
        Pattern.compile("(script|javascript|vbscript|onload|onerror)", Pattern.CASE_INSENSITIVE)
    )
    
    // NoSQL injection patterns (for Firebase/MongoDB)
    private val NOSQL_INJECTION_PATTERNS = listOf(
        Pattern.compile("\\$[a-zA-Z_]+", Pattern.CASE_INSENSITIVE), // MongoDB operators
        Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\{.*\\}", Pattern.CASE_INSENSITIVE), // Object injection
        Pattern.compile("\\[.*\\]", Pattern.CASE_INSENSITIVE), // Array injection
        Pattern.compile("function\\s*\\(", Pattern.CASE_INSENSITIVE),
        Pattern.compile("eval\\s*\\(", Pattern.CASE_INSENSITIVE)
    )
    
    /**
     * Check if input contains SQL injection patterns
     */
    fun containsSqlInjection(input: String): Boolean {
        return try {
            SQL_INJECTION_PATTERNS.any { pattern ->
                pattern.matcher(input).find()
            }
        } catch (e: Exception) {
            SecureLogger.e("SqlInjectionPrevention", "Error checking SQL injection", e)
            true // Fail secure - assume injection if error
        }
    }
    
    /**
     * Check if input contains NoSQL injection patterns
     */
    fun containsNoSqlInjection(input: String): Boolean {
        return try {
            NOSQL_INJECTION_PATTERNS.any { pattern ->
                pattern.matcher(input).find()
            }
        } catch (e: Exception) {
            SecureLogger.e("SqlInjectionPrevention", "Error checking NoSQL injection", e)
            true // Fail secure - assume injection if error
        }
    }
    
    /**
     * Validate input for SQL safety (strict validation)
     */
    fun isValidSqlInput(input: String): Boolean {
        return !containsSqlInjection(input) && 
               !containsNoSqlInjection(input) &&
               input.length <= 255 &&
               !input.contains("\\x00") && // Null byte
               !input.matches(Regex(".*[\\x00-\\x1F\\x7F].*")) // Control characters
    }
    
    /**
     * Sanitize input for SQL queries (use with caution - validation preferred)
     */
    fun sanitizeForSql(input: String): String {
        return try {
            input
                .replace("'", "''") // Escape single quotes
            .replace("\"", "\\\"") // Escape double quotes
            .replace("\\", "\\\\") // Escape backslashes
            .replace("\n", " ") // Replace newlines
            .replace("\r", " ") // Replace carriage returns
            .replace("\t", " ") // Replace tabs
            .replace("\u0000", "") // Remove null bytes
            .replace("--", "") // Remove SQL comments
            .replace("/*", "") // Remove SQL comments
            .replace("*/", "") // Remove SQL comments
            .replace(";", "") // Remove statement terminators
            .take(255) // Limit length
        } catch (e: Exception) {
            SecureLogger.e("SqlInjectionPrevention", "Error sanitizing SQL input", e)
            "" // Return empty string on error
        }
    }
    
    /**
     * Create safe parameterized query placeholder
     */
    fun createSafeParameter(value: String): String {
        if (!isValidSqlInput(value)) {
            throw IllegalArgumentException("Input contains potential SQL injection")
        }
        return value
    }
    
    /**
     * Validate multiple parameters for SQL safety
     */
    fun validateParameters(vararg parameters: String): Boolean {
        return try {
            // Authentication check for parameter validation
            if (!com.afilaxy.security.AuthGuard.isUserAuthenticated()) {
                SecureLogger.security("SQL_VALIDATION", "UNAUTHENTICATED_ACCESS")
                return false
            }
            parameters.all { isValidSqlInput(it) }
        } catch (e: Exception) {
            SecureLogger.e("SqlInjectionPrevention", "Error validating parameters", e)
            false
        }
    }
    
    /**
     * Safe string builder for dynamic queries (use with extreme caution)
     */
    class SafeQueryBuilder {
        private val query = StringBuilder()
        private val parameters = mutableListOf<String>()
        
        fun append(text: String): SafeQueryBuilder {
            // Only allow predefined safe SQL keywords and structures
            val safeText = when {
                text.matches(Regex("^(SELECT|FROM|WHERE|AND|OR|ORDER BY|GROUP BY|HAVING|LIMIT)$", RegexOption.IGNORE_CASE)) -> text
                text.matches(Regex("^[a-zA-Z_][a-zA-Z0-9_]*$")) -> text // Column/table names
                text == "?" -> text // Parameter placeholder
                else -> throw IllegalArgumentException("Unsafe SQL text: $text")
            }
            query.append(safeText).append(" ")
            return this
        }
        
        fun addParameter(value: String): SafeQueryBuilder {
            if (!isValidSqlInput(value)) {
                throw IllegalArgumentException("Parameter contains potential SQL injection")
            }
            parameters.add(value)
            return this
        }
        
        fun build(): Pair<String, List<String>> {
            return Pair(query.toString().trim(), parameters.toList())
        }
    }
    
    /**
     * Validate Firebase document path for injection
     */
    fun isValidFirebasePath(path: String): Boolean {
        return path.matches(Regex("^[a-zA-Z0-9_/-]+$")) &&
               !path.contains("..") &&
               !path.contains("//") &&
               path.length <= 1500 && // Firebase limit
               !containsNoSqlInjection(path)
    }
    
    /**
     * Validate Firebase field name
     */
    fun isValidFirebaseField(fieldName: String): Boolean {
        return fieldName.matches(Regex("^[a-zA-Z_][a-zA-Z0-9_]*$")) &&
               fieldName.length <= 1500 &&
               !fieldName.startsWith("__") // Reserved Firebase prefix
    }
}