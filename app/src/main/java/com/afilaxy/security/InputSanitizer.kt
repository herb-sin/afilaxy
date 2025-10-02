package com.afilaxy.security

import java.util.regex.Pattern

object InputSanitizer {
    
    private val ALPHANUMERIC_PATTERN = Pattern.compile("[^\\w\\s-]")
    private val EMAIL_PATTERN = Pattern.compile("[^\\w@.-]")
    private val NUMERIC_PATTERN = Pattern.compile("[^\\d.-]")
    
    fun sanitizeText(input: String?): String {
        return input?.let { 
            ALPHANUMERIC_PATTERN.matcher(it).replaceAll("")
        } ?: ""
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
        return input?.let {
            it.replace(Regex("[\\r\\n\\t]"), "")
              .replace(Regex("[^\\w\\s.-]"), "")
              .take(100)
        } ?: ""
    }
}