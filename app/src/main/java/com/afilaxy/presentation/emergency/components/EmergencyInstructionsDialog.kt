package com.afilaxy.presentation.emergency.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun EmergencyInstructionsDialog(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "🚨 Instruções de Emergência",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Enquanto aguarda ajuda:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text("• Mantenha-se calmo e respire devagar")
                Text("• Sente-se em posição confortável")
                Text("• Afrouxe roupas apertadas")
                Text("• Se possível, vá para local arejado")
                Text("• Evite esforços físicos")
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "⚠️ Sinais de alerta:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.error
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text("• Dificuldade extrema para respirar")
                Text("• Lábios ou unhas azulados")
                Text("• Confusão mental")
                Text("• Dor no peito intensa")
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Se apresentar estes sintomas, chame 192 (SAMU) imediatamente!",
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Entendi")
                }
            }
        }
    }
}