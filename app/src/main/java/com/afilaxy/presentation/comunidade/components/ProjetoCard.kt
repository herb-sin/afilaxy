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
import com.afilaxy.domain.model.ProjetoInfo
import com.afilaxy.ui.theme.AfilaxyTheme

@Composable
fun ProjetoCard(info: ProjetoInfo) {
    Card(
        modifier = Modifier
            .width(180.dp)
            .heightIn(min = 100.dp)
            .padding(vertical = 8.dp),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(info.titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(info.texto, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Preview
@Composable
fun ProjetoCardPreview() {
    AfilaxyTheme {
        ProjetoCard(ProjetoInfo(titulo = "Sobre o Afilaxy", texto = "Projeto social para conectar pacientes e voluntários."))
    }
}