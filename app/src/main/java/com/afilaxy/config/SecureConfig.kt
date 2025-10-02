package com.afilaxy.config

object SecureConfig {
    
    // Configurações de segurança
    const val ENABLE_SECURITY_LOGGING = false
    const val MAX_LOGIN_ATTEMPTS = 3
    const val RATE_LIMIT_WINDOW_MS = 60_000L // 1 minuto
    const val MAX_REQUESTS_PER_WINDOW = 10
}