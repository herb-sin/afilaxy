package com.afilaxy.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.afilaxy.domain.model.Helper

@Composable
fun HelperRatingDialog(
    helper: Helper,
    onRatingSubmitted: (Int, String) -> Unit,
    onDismiss: () -> Unit
) {
    var rating by remember { mutableStateOf(0) }
    var feedback by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Avaliar Ajuda Recebida") },
        text = {
            Column {
                Text(
                    text = "Como foi a ajuda de ${helper.nome}?",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                RatingBar(
                    rating = rating,
                    onRatingChanged = { rating = it }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = feedback,
                    onValueChange = { feedback = it },
                    label = { Text("Comentário (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { 
                    if (rating > 0) {
                        onRatingSubmitted(rating, feedback)
                    }
                },
                enabled = rating > 0
            ) {
                Text("Enviar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun RatingBar(
    rating: Int,
    onRatingChanged: (Int) -> Unit,
    maxRating: Int = 5
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        repeat(maxRating) { index ->
            val starIndex = index + 1
            Icon(
                imageVector = if (starIndex <= rating) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = "Star $starIndex",
                tint = if (starIndex <= rating) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                modifier = Modifier
                    .size(32.dp)
                    .clickable { onRatingChanged(starIndex) }
                    .padding(2.dp)
            )
        }
    }
}