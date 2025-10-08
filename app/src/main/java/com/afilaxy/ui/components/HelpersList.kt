package com.afilaxy.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.afilaxy.domain.model.Helper
import com.afilaxy.presentation.emergency.components.HelperCard

@Composable
fun HelpersList(
    helpers: List<Helper>,
    onHelperClick: (Helper) -> Unit = {},
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(helpers) { helper ->
            Card(
                onClick = { onHelperClick(helper) },
                modifier = Modifier.fillMaxWidth()
            ) {
                HelperCard(helper = helper)
            }
        }
    }
}