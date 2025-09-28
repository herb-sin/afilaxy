package com.afilaxy.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.io.BufferedReader
import java.io.InputStreamReader

@Composable
fun TermsScreen() {
    val context = LocalContext.current
    val termsText = remember { loadMarkdownFromAssets(context, "TERMOS_DE_USO.md") }

    Column(modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState())) {
        Text(
                text = "Termos Gerais e Condições de Uso",
                style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(termsText)
    }
}

fun loadMarkdownFromAssets(context: Context, fileName: String): String {
    return try {
        val inputStream = context.assets.open(fileName)
        val reader = BufferedReader(InputStreamReader(inputStream))
        reader.use { it.readText() }
    } catch (e: Exception) {
        "Erro ao carregar os Termos de Uso."
    }
}
