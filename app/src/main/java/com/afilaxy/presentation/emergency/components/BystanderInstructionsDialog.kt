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
fun BystanderInstructionsDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                    modifier =
                            Modifier.fillMaxWidth()
                                    .padding(16.dp)
                                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                        text = "🆘 Instruções para Transeuntes",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                        text = "Esta pessoa está em CRISE DE ASMA",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                        text = "✅ Situação atual:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text("• Uma pessoa com 'bombinha' está a caminho")

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                        text = "🚨 SE A CRISE FOR MUITO FORTE:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                )

                Spacer(modifier = Modifier.height(8.dp))

                Card(
                        colors =
                                CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer
                                )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = "LIGUE 192 (SAMU) e informe:", fontWeight = FontWeight.Bold)
                        Text("• Crise de asma em andamento")
                        Text("• Localização exata")
                        Text("• Estado do paciente")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                        text = "Como ajudar enquanto aguarda:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text("• Mantenha a calma e tranquilize o paciente")
                Text("• Ajude-o a sentar-se confortavelmente")
                Text("• Garanta ventilação no local")
                Text("• Não deixe o paciente sozinho")

                Spacer(modifier = Modifier.height(24.dp))

                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Entendi") }
            }
        }
    }
}
