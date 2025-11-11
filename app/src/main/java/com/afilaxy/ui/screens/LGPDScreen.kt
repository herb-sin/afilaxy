package com.afilaxy.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.afilaxy.privacy.PrivacyInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LGPDScreen(navController: NavController? = null) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Política de Privacidade") },
                navigationIcon = {
                    IconButton(onClick = { navController?.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
        Text(
            text = "Política de Privacidade e LGPD",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(12.dp))

        Text("Este aplicativo segue a Lei Geral de Proteção de Dados Pessoais (LGPD), garantindo segurança, privacidade e transparência aos usuários.")
        Spacer(modifier = Modifier.height(18.dp))

        Text("Seus direitos:", style = MaterialTheme.typography.titleMedium)
        PrivacyInfo.LGPD_RIGHTS.forEach { right ->
            Text(
                text = "• $right",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
        Spacer(modifier = Modifier.height(18.dp))

        Text("Para exercer seus direitos, envie sua solicitação para:", style = MaterialTheme.typography.bodyMedium)
        Text(PrivacyInfo.DPO_EMAIL, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(12.dp))

        Text("Encarregado pelo Tratamento de Dados (DPO):", style = MaterialTheme.typography.titleSmall)
        Text("Nome: ${PrivacyInfo.DPO_NAME}", style = MaterialTheme.typography.bodyMedium)
        Text("E-mail: ${PrivacyInfo.DPO_EMAIL}", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(12.dp))

        Text("Incidentes de segurança serão comunicados por e-mail e pelo app.")
        Text("Dados sensíveis só são tratados mediante consentimento explícito.")
            Text("Exclusão dos dados pode ser solicitada pelo canal de atendimento.")
        }
    }
}