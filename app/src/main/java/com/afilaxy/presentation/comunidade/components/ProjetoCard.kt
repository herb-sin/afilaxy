package com.afilaxy.presentation.comunidade.components

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.afilaxy.domain.model.ProjetoInfo
import com.afilaxy.ui.theme.AfilaxyTheme
import android.content.Intent
import android.net.Uri

@Composable
fun ProjetoCard(info: ProjetoInfo) {
    val context = LocalContext.current
    
    Card(
        modifier = Modifier
            .width(180.dp)
            .heightIn(min = 100.dp)
            .padding(horizontal = 4.dp, vertical = 8.dp)
            .clickable {
                val url = when {
                    info.titulo.contains("Site Afilaxy") -> "https://afilaxy.com/"
                    info.titulo.contains("Asma no SUS") -> "https://www.gov.br/saude/pt-br/assuntos/saude-de-a-a-z/a/asma"
                    info.titulo.contains("Parque Tecnológico") -> "https://fpts.org.br/"
                    else -> null
                }
                url?.let {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it))
                    context.startActivity(intent)
                }
            },
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(info.titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (info.texto.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(info.texto, style = MaterialTheme.typography.bodySmall)
            }
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