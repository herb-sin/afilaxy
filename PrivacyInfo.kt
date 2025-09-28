package com.afilaxy.privacy

/**
 * Exibe informações de privacidade e canal LGPD no app.
 */
object PrivacyInfo {
    const val DPO_EMAIL = "afilaxy@gmail.com"
    const val DPO_NAME = "Herbert Jung Sin"

    val LGPD_RIGHTS: List<String> = listOf(
        "Solicitar acesso aos dados",
        "Corrigir dados pessoais",
        "Excluir dados",
        "Anonimizar dados",
        "Portabilizar dados",
        "Revogar consentimento"
    )
}