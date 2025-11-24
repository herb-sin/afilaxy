package com.afilaxy.security

interface InputValidator {
    fun isValidCoordinate(lat: Double, lon: Double): Boolean
}

class SecurityInputValidator : InputValidator {
    override fun isValidCoordinate(lat: Double, lon: Double): Boolean = SecurityUtils.isValidCoordinate(lat, lon)
}
