package com.afilaxy.presentation.comunidade

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.afilaxy.presentation.comunidade.components.EventoCard
import com.afilaxy.presentation.comunidade.components.ProdutoCard
import com.afilaxy.presentation.comunidade.components.ProjetoCard
import com.afilaxy.ui.theme.AfilaxyTheme

@Composable
fun ComunidadeScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: ComunidadeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isLoading) {
        Column(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Carregando comunidade...")
        }
        return
    }

    if (uiState.errorMessage != null) {
        val errorMsg = uiState.errorMessage
        Column(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Ocorreu um erro inesperado. Tente novamente.",
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { navController.popBackStack() }) {
                Text("Voltar")
            }
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text("Produtos", style = MaterialTheme.typography.titleLarge)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(uiState.produtos) { produto ->
                ProdutoCard(produto)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        Text("Eventos", style = MaterialTheme.typography.titleLarge)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(uiState.eventos) { evento ->
                EventoCard(evento)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        Text("Sobre o Projeto", style = MaterialTheme.typography.titleLarge)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(uiState.projetos) { info ->
                ProjetoCard(info)
            }
        }
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Voltar para Tela Inicial")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ComunidadeScreenPreview() {
    AfilaxyTheme {
        ComunidadeScreen(navController = rememberNavController())
    }
}