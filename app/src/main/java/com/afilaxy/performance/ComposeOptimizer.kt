package com.afilaxy.performance

import androidx.compose.runtime.*

/**
 * Otimizador específico para Compose
 */
object ComposeOptimizer {
    
    /**
     * LaunchedEffect com debounce
     */
    @Composable
    fun DebouncedEffect(
        key: Any?,
        delayMs: Long = 300,
        block: suspend () -> Unit
    ) {
        LaunchedEffect(key) {
            kotlinx.coroutines.delay(delayMs)
            block()
        }
    }
}