package com.afilaxy.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import java.io.BufferedReader
import java.io.InputStreamReader

@Composable
fun TermsScreen() {
    val context = LocalContext.current
    val markdownContent = loadMarkdownFromAssets(context, "TERMS_DE_USO.md")
    Text(text = markdownContent, fontSize = 16.sp, fontWeight = FontWeight.Normal, modifier = Modifier.fillMaxSize())
}

fun loadMarkdownFromAssets(context: Context, fileName: String): String {
    val inputStream = context.assets.open(fileName)
    val reader = BufferedReader(InputStreamReader(inputStream))
    return reader.use { it.readText() }
}

@Preview
@Composable
fun PreviewTermsScreen() {
    TermsScreen()
}