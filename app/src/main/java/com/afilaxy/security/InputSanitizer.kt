package com.afilaxy.security

import java.util.regex.Pattern

object InputSanitizer {
    
    private val ALPHANUMERIC_PATTERN = Pattern.compile("[^\\w\\s-àáâãäåæçèéêëìíîïðñòóôõöøùúûüýþÿ]")
    private val EMAIL_PATTERN = Pattern.compile("[^\\w@.-]")
    private val NUMERIC_PATTERN = Pattern.compile("[^\\d.-]")
    private val NOSQL_INJECTION_PATTERN = Pattern.compile("[{}$\\[\\]()'\";]")
    private val LOG_INJECTION_PATTERN = Pattern.compile("[\\r\\n\\t\\x00-\\x1f\\x7f-\\x9f]")
    
    fun sanitizeText(input: String?): String {
        return input?.let { text ->
            NOSQL_INJECTION_PATTERN.matcher(
                ALPHANUMERIC_PATTERN.matcher(text).replaceAll("")
            ).replaceAll("")
        }?.trim() ?: ""
    }
    
    fun sanitizeEmail(input: String?): String {
        return input?.let {
            EMAIL_PATTERN.matcher(it).replaceAll("")
        } ?: ""
    }
    
    fun sanitizeNumeric(input: String?): String {
        return input?.let {
            NUMERIC_PATTERN.matcher(it).replaceAll("")
        } ?: ""
    }
    
    fun sanitizeForLog(input: String?): String {
        return input?.let { text ->
            LOG_INJECTION_PATTERN.matcher(text).replaceAll("")
                .replace(Regex("[^\\w\\s.-@]"), "")
                .take(100)
        }?.trim() ?: ""
    }
    
    fun sanitizeForFirestore(input: String?): String {
        return input?.let { text ->
            NOSQL_INJECTION_PATTERN.matcher(text).replaceAll("")
                .replace(".", "")
                .replace("#", "")
                .replace("[", "")
                .replace("]", "")
        }?.trim() ?: ""
    }
}