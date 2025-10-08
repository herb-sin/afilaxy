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
        return !containsSqlInjection(input) && !containsPathTraversal(input)
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
    fun createSecureDocumentBuilder(): DocumentBuilderFactory {
        return DocumentBuilderFactory.newInstance().apply {
            setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
            setFeature("http://xml.org/sax/features/external-general-entities", false)
            setFeature("http://xml.org/sax/features/external-parameter-entities", false)
            setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
            isXIncludeAware = false
            isExpandEntityReferences = false
        }
    }
    
    // Safe file extensions
    private val SAFE_EXTENSIONS = setOf(".jpg", ".jpeg", ".png", ".gif", ".pdf", ".txt")
    
    fun validateFileExtension(filename: String): Boolean {
        val extension = filename.substringAfterLast('.', "").lowercase()
        return SAFE_EXTENSIONS.contains(".$extension")
    }
    
    // File path validation
    fun validateFilePath(path: String): Boolean {
        return try {
            val file = File(path).canonicalFile
            val allowedDir = File("/data/data/com.afilaxy").canonicalFile
            file.path.startsWith(allowedDir.path) && validateFileExtension(file.name)
        } catch (e: Exception) {
            Log.e(TAG, "Path validation error", e)
            false
        }
    }
}