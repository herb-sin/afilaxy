package com.afilaxy.presentation.emergency.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.afilaxy.domain.model.Helper
import com.afilaxy.ui.theme.AfilaxyTheme

@Composable
fun HelperCard(helper: Helper) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 8.dp)
    ) {
        Text(helper.nome, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(4.dp))
        Text("Localização: ${helper.distanciaEstimada}", style = MaterialTheme.typography.bodyMedium)
        // TODO: Adicionar botão para contatar no futuro
    }
}

@Preview
@Composable
fun HelperCardPreview() {
    AfilaxyTheme {
        HelperCard(Helper(id = "1", nome = "Ajudante Voluntário A", distanciaEstimada = "aprox. 150m"))
    }
}