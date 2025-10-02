package com.afilaxy.security

import android.util.Patterns

object InputValidator {
    
    data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String? = null
    )
    
    fun validateEmail(email: String?): ValidationResult {
        return when {
            email.isNullOrBlank() -> ValidationResult(false, "Email é obrigatório")
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> ValidationResult(false, "Email inválido")
            email.length > 100 -> ValidationResult(false, "Email muito longo")
            else -> ValidationResult(true)
        }
    }
    
    fun validatePassword(password: String?): ValidationResult {
        return when {
            password.isNullOrBlank() -> ValidationResult(false, "Senha é obrigatória")
            password.length < 6 -> ValidationResult(false, "Senha deve ter pelo menos 6 caracteres")
            password.length > 50 -> ValidationResult(false, "Senha muito longa")
            else -> ValidationResult(true)
        }
    }
    
    fun validateName(name: String?): ValidationResult {
        return when {
            name.isNullOrBlank() -> ValidationResult(false, "Nome é obrigatório")
            name.length < 2 -> ValidationResult(false, "Nome muito curto")
            name.length > 50 -> ValidationResult(false, "Nome muito longo")
            !name.matches(Regex("^[a-zA-ZÀ-ÿ\\s]+$")) -> ValidationResult(false, "Nome deve conter apenas letras")
            else -> ValidationResult(true)
        }
    }
    
    fun validateRequired(value: String?, fieldName: String): ValidationResult {
        return when {
            value.isNullOrBlank() -> ValidationResult(false, "$fieldName é obrigatório")
            else -> ValidationResult(true)
        }
    }
    
    fun sanitizeAndValidate(input: String?, validator: (String?) -> ValidationResult): ValidationResult {
        val sanitized = InputSanitizer.sanitizeText(input)
        return validator(sanitized)
    }
}