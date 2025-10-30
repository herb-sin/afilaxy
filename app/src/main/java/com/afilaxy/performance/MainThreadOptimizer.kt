package com.afilaxy.performance

import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.*

/**
 * Otimizador específico para evitar ANRs na thread principal
 */
object MainThreadOptimizer {
    
    private val backgroundScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val mainHandler = Handler(Looper.getMainLooper())
    
    /**
     * Executa operação com delay para não bloquear UI
     */
    fun executeWithDelay(delayMs: Long = 100, operation: () -> Unit) {
        mainHandler.postDelayed(operation, delayMs)
    }
    
    /**
     * Executa operação em chunks para não bloquear thread principal
     */
    fun executeInChunks(
        items: List<Any>,
        chunkSize: Int = 10,
        operation: (Any) -> Unit,
        onComplete: () -> Unit = {}
    ) {
        backgroundScope.launch {
            items.chunked(chunkSize).forEach { chunk ->
                chunk.forEach { operation(it) }
                delay(16) // 1 frame delay
            }
            withContext(Dispatchers.Main) {
                onComplete()
            }
        }
    }
    
    /**
     * Força yield da thread principal
     */
    fun yieldMainThread() {
        mainHandler.post { }
    }
}