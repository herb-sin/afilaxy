package com.afilaxy.security

import com.afilaxy.utils.ErrorHandler
import java.util.regex.Pattern

object InputSanitizer {
    
    // Padrões seguros para diferentes tipos de entrada
    private val SAFE_TEXT_PATTERN = Pattern.compile("[^\\p{L}\\p{N}\\s._-]")
    private val SAFE_EMAIL_PATTERN = Pattern.compile("[^\\w@._-]")
    private val SAFE_NUMERIC_PATTERN = Pattern.compile("[^\\d.-]")
    
    // Padrões perigosos para NoSQL e Log injection
    private val DANGEROUS_CHARS = Pattern.compile("[{}$\\[\\]()'\";\\\\<>\"&|`~!#%^*+=?/]")
    private val CONTROL_CHARS = Pattern.compile("[\\p{Cntrl}]")
    private val LOG_INJECTION_PATTERN = Pattern.compile("[\\r\\n\\t]")
    
    fun sanitizeText(input: String?): String {
        return ErrorHandler.safeOperation {
            input?.let { text ->
                val cleaned = CONTROL_CHARS.matcher(text).replaceAll("")
                DANGEROUS_CHARS.matcher(cleaned).replaceAll("")
            }?.trim()?.take(200) ?: ""
        } ?: ""
    }
    
    fun sanitizeEmail(input: String?): String {
        return ErrorHandler.safeOperation {
            input?.let { email ->
                val cleaned = CONTROL_CHARS.matcher(email).replaceAll("")
                SAFE_EMAIL_PATTERN.matcher(cleaned).replaceAll("")
            }?.trim()?.take(100) ?: ""
        } ?: ""
    }
    
    fun sanitizeNumeric(input: String?): String {
        return ErrorHandler.safeOperation {
            input?.let {
                SAFE_NUMERIC_PATTERN.matcher(it).replaceAll("")
            } ?: ""
        } ?: ""
    }
    
    fun sanitizeForLog(input: String?): String {
        return ErrorHandler.safeOperation {
            input?.let { text ->
                LOG_INJECTION_PATTERN.matcher(text).replaceAll("")
                    .replace(Regex("[^\\w\\s.-@]"), "")
                    .take(100)
            }?.trim() ?: ""
        } ?: ""
    }
    
    fun sanitizeForFirestore(input: String?): String {
        return ErrorHandler.safeOperation {
            input?.let { text ->
                val cleaned = CONTROL_CHARS.matcher(text).replaceAll("")
                DANGEROUS_CHARS.matcher(cleaned).replaceAll("")
                    .replace("/", "")
                    .replace(".", "")
                    .replace("__", "_")
            }?.trim()?.take(100) ?: ""
        } ?: ""
    }
}