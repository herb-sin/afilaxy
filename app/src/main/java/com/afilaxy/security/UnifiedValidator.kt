package com.afilaxy.security

import android.util.Patterns
import java.util.regex.Pattern

object UnifiedValidator {
    
    private val SQL_INJECTION_PATTERNS = listOf(
        Pattern.compile("('|(\\-\\-)|(;)|(\\|)|(\\*)|(%))", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(union|select|insert|update|delete|drop|create|alter)", Pattern.CASE_INSENSITIVE)
    )
    
    sealed class ValidationResult {
        object Valid : ValidationResult()
        data class Invalid(val errors: List<String>) : ValidationResult()
    }
    
    fun validateEmail(email: String?): ValidationResult {
        return when {
            email.isNullOrBlank() -> ValidationResult.Invalid(listOf("Email é obrigatório"))
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> ValidationResult.Invalid(listOf("Email inválido"))
            email.length > 100 -> ValidationResult.Invalid(listOf("Email muito longo"))
            else -> ValidationResult.Valid
        }
    }
    
    fun validatePassword(password: String?): ValidationResult {
        return when {
            password.isNullOrBlank() -> ValidationResult.Invalid(listOf("Senha é obrigatória"))
            password.length < 6 -> ValidationResult.Invalid(listOf("Senha deve ter pelo menos 6 caracteres"))
            password.length > 50 -> ValidationResult.Invalid(listOf("Senha muito longa"))
            else -> ValidationResult.Valid
        }
    }
    
    fun validateName(name: String?): ValidationResult {
        return when {
            name.isNullOrBlank() -> ValidationResult.Invalid(listOf("Nome é obrigatório"))
            name.length < 2 -> ValidationResult.Invalid(listOf("Nome muito curto"))
            name.length > 50 -> ValidationResult.Invalid(listOf("Nome muito longo"))
            !name.matches(Regex("^[a-zA-ZÀ-ÿ\\s]+$")) -> ValidationResult.Invalid(listOf("Nome deve conter apenas letras"))
            else -> ValidationResult.Valid
        }
    }
    
    fun validateCoordinates(lat: Double, lon: Double): Boolean {
        return lat in -90.0..90.0 && lon in -180.0..180.0
    }
    
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
        return input.contains("../") || input.contains("..\\")
    }
}