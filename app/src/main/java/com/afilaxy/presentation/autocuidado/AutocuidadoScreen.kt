package com.afilaxy.presentation.autocuidado

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutocuidadoScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ℹ️ Informações") },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() }
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(16.dp)
        ) {

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Como usar o Afilaxy",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Caso entre em crise de Asma sem estar com a 'bombinha':\n" +
                            "• Mostre essa mensagem para uma pessoa próxima;\n" +
                            "• Peça para essa pessoa ligar para 192 SAMU e explicar a situação;\n" +
                            "• Volte a tela anterior e acesse o pedido de socorro no botão EMERGÊNCIA;\n" +
                            "• Pressione o botão 'SOLICITAR AJUDA';\n" +
                            "• Aguarde o app encontrar alguém. Uma tela com mapa da sua localização e da pessoa será aberto, clique no chat para conversar diretamente com quem aceitou ajudar;\n" +
                            "• Aguarde o SAMU e a pessoa com a 'bombinha'.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "🏥 Asma no SUS",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "O tratamento da asma é garantido pelo SUS (Sistema Único de Saúde), incluindo:\n\n" +
                            "✅ Consultas com pneumologista\n" +
                            "✅ Medicamentos gratuitos (broncodilatadores e corticoides)\n" +
                            "✅ Acompanhamento em Unidades Básicas de Saúde\n" +
                            "✅ Atendimento de emergência pelo SAMU (192)\n\n" +
                            "Procure a UBS mais próxima para iniciar seu tratamento!",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.gov.br/saude/pt-br/assuntos/saude-de-a-a-z/a/asma"))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("🔗 Mais informações no site do Ministério da Saúde")
                }
            }
        }
        
        }
    }
}