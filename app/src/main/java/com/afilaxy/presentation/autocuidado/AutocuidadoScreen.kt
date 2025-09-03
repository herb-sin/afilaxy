package com.afilaxy.presentation.autocuidado

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.afilaxy.UiState
import com.afilaxy.ui.theme.AfilaxyTheme

@Composable
fun AutocuidadoScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: AutocuidadoViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Assistente de Autocuidado",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Digite sua dúvida sobre Asma ou DPOC e receba orientações seguras.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        TextField(
            value = uiState.pergunta,
            onValueChange = { viewModel.updatePergunta(it) },
            label = { Text("Pergunte sobre Asma ou DPOC") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { viewModel.perguntarIA() },
            enabled = uiState.pergunta.isNotBlank()
        ) {
            Text("Perguntar")
        }
        Spacer(modifier = Modifier.height(24.dp))

        // Bloco rolável apenas para a resposta da IA
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            RespostaIA(uiState.resposta)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Botão fora do bloco rolável, sempre visível
        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Voltar para Tela Inicial")
        }
    }
}

@Composable
fun RespostaIA(uiState: UiState) {
    val scrollState = rememberScrollState()

    when (uiState) {
        is UiState.Loading -> {
            CircularProgressIndicator()
        }
        is UiState.Success -> {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp, max = 400.dp) // Limita a altura para permitir rolagem
                    .verticalScroll(scrollState)
                    .padding(8.dp)
            ) {
                Text(
                    text = uiState.resumo,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        is UiState.Error -> {
            Text(
                text = "Erro: ${uiState.message}",
                color = MaterialTheme.colorScheme.error
            )
        }
        else -> {}
    }
}

@Preview(showBackground = true)
@Composable
fun AutocuidadoScreenPreview() {
    AfilaxyTheme {
        AutocuidadoScreen(navController = rememberNavController())
    }
}