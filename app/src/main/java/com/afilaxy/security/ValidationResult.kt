package com.afilaxy.security

sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Invalid(val errors: List<String>) : ValidationResult()
    
    fun isValid(): Boolean = this is Valid
    

}

object ValidationHelper {
    
    fun validateEmail(email: String?): ValidationResult {
        val errors = mutableListOf<String>()
        
        if (email.isNullOrBlank()) {
            errors.add("Email é obrigatório")
            return ValidationResult.Invalid(errors)
        }
        
        val sanitized = InputSanitizer.sanitizeEmail(email)
        if (sanitized.isEmpty()) {
            errors.add("Formato de email inválido")
        }
        
        if (sanitized.length > 254) {
            errors.add("Email muito longo")
        }
        
        return if (errors.isEmpty()) ValidationResult.Valid else ValidationResult.Invalid(errors)
    }
    
    fun validatePassword(password: String?): ValidationResult {
        val errors = mutableListOf<String>()
        
        if (password.isNullOrBlank()) {
            errors.add("Senha é obrigatória")
            return ValidationResult.Invalid(errors)
        }
        
        if (password.length < 8) {
            errors.add("Senha deve ter pelo menos 8 caracteres")
        }
        
        if (!password.any { it.isUpperCase() }) {
            errors.add("Senha deve conter pelo menos uma letra maiúscula")
        }
        
        if (!password.any { it.isLowerCase() }) {
            errors.add("Senha deve conter pelo menos uma letra minúscula")
        }
        
        if (!password.any { it.isDigit() }) {
            errors.add("Senha deve conter pelo menos um número")
        }
        
        return if (errors.isEmpty()) ValidationResult.Valid else ValidationResult.Invalid(errors)
    }
    
    fun validateName(name: String?): ValidationResult {
        val errors = mutableListOf<String>()
        
        if (name.isNullOrBlank()) {
            errors.add("Nome é obrigatório")
            return ValidationResult.Invalid(errors)
        }
        
        val sanitized = InputSanitizer.sanitizeName(name)
        if (sanitized.isEmpty()) {
            errors.add("Nome contém caracteres inválidos")
        }
        
        if (sanitized.length < 2) {
            errors.add("Nome deve ter pelo menos 2 caracteres")
        }
        
        return if (errors.isEmpty()) ValidationResult.Valid else ValidationResult.Invalid(errors)
    }
    
    fun validateCoordinates(lat: Double?, lon: Double?): ValidationResult {
        val errors = mutableListOf<String>()
        
        if (lat == null || lon == null) {
            errors.add("Coordenadas são obrigatórias")
            return ValidationResult.Invalid(errors)
        }
        
        if (!SecurityUtils.isValidCoordinate(lat, lon)) {
            errors.add("Coordenadas inválidas")
        }
        
        return if (errors.isEmpty()) ValidationResult.Valid else ValidationResult.Invalid(errors)
    }
}