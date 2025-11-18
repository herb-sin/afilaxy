package com.afilaxy.presentation.comunidade.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.afilaxy.domain.model.Produto
import com.afilaxy.ui.theme.AfilaxyTheme

@Composable
fun ProdutoCard(
    produto: Produto,
    onClick: () -> Unit = {}
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .width(200.dp)
            .heightIn(min = 160.dp)
            .padding(horizontal = 4.dp, vertical = 8.dp),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Nome do produto
            Text(
                text = produto.nome, 
                style = MaterialTheme.typography.titleMedium, 
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Farmácia
            produto.farmacia?.let {
                Text(
                    text = "📍 $it",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(6.dp))
            
            // Preços e desconto
            produto.desconto?.let {
                Text(
                    text = "🏷️ $it",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Preço atual e original
            if (produto.preco != null) {
                Text(
                    text = "R$ ${produto.preco}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            // Cupom
            produto.cupom?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "🎫 Cupom: $it",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Preview
@Composable
fun ProdutoCardPreview() {
    AfilaxyTheme {
        ProdutoCard(Produto(nome = "Bombinha Portátil", descricao = "Ideal para emergências"))
    }
}