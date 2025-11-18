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
import com.afilaxy.domain.model.Evento
import com.afilaxy.ui.theme.AfilaxyTheme

@Composable
fun EventoCard(
    evento: Evento,
    onClick: () -> Unit = {}
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .width(200.dp)
            .heightIn(min = 140.dp)
            .padding(horizontal = 4.dp, vertical = 8.dp),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = evento.titulo, 
                style = MaterialTheme.typography.titleMedium, 
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "📅 ${evento.data}", 
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
            
            evento.horario?.let {
                Text(
                    text = "🕰️ $it",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            Spacer(modifier = Modifier.height(6.dp))
            
            evento.organizador?.let {
                Text(
                    text = "🏢 $it",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            
            evento.local?.let {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "📍 $it",
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Preview
@Composable
fun EventoCardPreview() {
    AfilaxyTheme {
        EventoCard(Evento(titulo = "Live: Cuidados com Asma", data = "20/08/2025"))
    }
}