package com.afilaxy.presentation.login

fun translateFirebaseError(error: String?): String {
    if (error.isNullOrBlank()) {
        return "Erro desconhecido. Tente novamente."
    }
    
    return when {
        error.contains("The email address is badly formatted") -> "Formato de e-mail inválido"
        error.contains("Password should be at least 6 characters") -> "A senha deve ter pelo menos 6 caracteres"
        error.contains("The password is invalid") -> "Senha inválida"
        error.contains("There is no user record") -> "Email não registrado, crie sua conta!"
        error.contains("The email address is already in use") -> "E-mail já cadastrado"
        error.contains("A network error") -> "Erro de rede"
        error.contains("An internal error has occurred") -> "Erro interno do Firebase. Verifique sua conexão."
        error.contains("API key not valid") -> "Chave API inválida. Verifique a configuração do Firebase."
        error.contains("CONFIGURATION_NOT_FOUND") -> "Configuração do Firebase não encontrada."
        error.contains("NETWORK_ERROR") -> "Erro de rede. Verifique sua conexão com a internet."
        error.contains("INVALID_LOGIN_CREDENTIALS") -> "Email não registrado, crie sua conta!"
        error.contains("The supplied auth credential is incorrect, malformed or has expired") -> "Email não registrado, crie sua conta!"
        error.contains("TOO_MANY_ATTEMPTS_TRY_LATER") -> "Muitas tentativas. Tente novamente mais tarde"
        else -> "Erro de autenticação. Verifique suas credenciais."
    }
}