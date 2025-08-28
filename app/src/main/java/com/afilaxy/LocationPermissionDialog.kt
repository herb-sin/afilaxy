package com.afilaxy

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun LocationPermissionDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Permissão de Localização") },
        text = { Text("Precisamos da sua localização para conectar você a pessoas próximas e facilitar pedidos de ajuda. Sua privacidade será respeitada.") },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("Permitir") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Agora não") }
        }
    )
}