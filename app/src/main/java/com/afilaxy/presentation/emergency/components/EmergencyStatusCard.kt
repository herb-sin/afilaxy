package com.afilaxy.presentation.emergency.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.afilaxy.domain.model.Emergency
import com.afilaxy.domain.model.EmergencyStatus

@Composable
fun EmergencyStatusCard(
    emergency: Emergency,
    helpersCount: Int = 0,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (emergency.status) {
                EmergencyStatus.ACTIVE -> MaterialTheme.colorScheme.errorContainer
                EmergencyStatus.HELPER_RESPONDING -> MaterialTheme.colorScheme.primaryContainer
                EmergencyStatus.RESOLVED -> MaterialTheme.colorScheme.tertiaryContainer
                EmergencyStatus.CANCELLED -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = getStatusText(emergency.status),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = getStatusEmoji(emergency.status),
                    style = MaterialTheme.typography.headlineSmall
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (emergency.status == EmergencyStatus.ACTIVE && helpersCount > 0) {
                Text(
                    text = "Pedido de ajuda enviado! $helpersCount pessoas próximas foram notificadas.",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                Text(
                    text = emergency.description,
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "Solicitado por: ${emergency.userName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun getStatusText(status: EmergencyStatus): String {
    return when (status) {
        EmergencyStatus.ACTIVE -> "Emergência Ativa"
        EmergencyStatus.HELPER_RESPONDING -> "Ajuda a Caminho"
        EmergencyStatus.RESOLVED -> "Emergência Resolvida"
        EmergencyStatus.CANCELLED -> "Emergência Cancelada"
    }
}

private fun getStatusEmoji(status: EmergencyStatus): String {
    return when (status) {
        EmergencyStatus.ACTIVE -> "🆘"
        EmergencyStatus.HELPER_RESPONDING -> "🏃‍♂️"
        EmergencyStatus.RESOLVED -> "✅"
        EmergencyStatus.CANCELLED -> "❌"
    }
}