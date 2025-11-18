package com.afilaxy.presentation.comunidade

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.afilaxy.presentation.comunidade.components.ProdutoCard
import com.afilaxy.presentation.comunidade.components.EventoCard
import com.afilaxy.presentation.comunidade.components.ProjetoCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComunidadeScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: ComunidadeViewModel = viewModel()
) {
    @Composable
    fun InfoCard(title: String, description: String) {
        Card(
            modifier = Modifier.width(200.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🌍 Comunidade") },
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Bem-vindo à Comunidade Afilaxy!",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(24.dp))
            
            // Produtos Carrossel
            Text(
                "📊 Produtos",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(viewModel.produtos, key = { it.id }) { produto ->
                    ProdutoCard(
                        produto = produto,
                        onClick = {
                            navController.navigate("produto_detail/${produto.id}")
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Eventos Carrossel
            Text(
                "📅 Eventos",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(viewModel.eventos, key = { it.id }) { evento ->
                    EventoCard(
                        evento = evento,
                        onClick = {
                            navController.navigate("evento_detail/${evento.id}")
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Sobre o Projeto Carrossel
            Text(
                "🎆 Sobre o Projeto",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(viewModel.projetos, key = { it.id }) { projeto ->
                    ProjetoCard(info = projeto)
                }
            }
        }
    }
}

