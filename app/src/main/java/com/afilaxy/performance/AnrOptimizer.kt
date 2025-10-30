package com.afilaxy.performance

import android.content.Context
import kotlinx.coroutines.*
import java.util.concurrent.Executors

/**
 * Otimizador para prevenir ANRs (Application Not Responding)
 */
object AnrOptimizer {
    
    private val backgroundExecutor = Executors.newCachedThreadPool()
    private val ioScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    /**
     * Executa operação pesada em background thread
     */
    fun executeInBackground(operation: () -> Unit) {
        backgroundExecutor.execute {
            try {
                operation()
            } catch (e: Exception) {
                android.util.Log.e("AnrOptimizer", "Background operation failed", e)
            }
        }
    }
    
    /**
     * Executa operação com coroutine no IO dispatcher
     */
    fun executeAsync(operation: suspend () -> Unit) {
        ioScope.launch {
            try {
                operation()
            } catch (e: Exception) {
                android.util.Log.e("AnrOptimizer", "Async operation failed", e)
            }
        }
    }
    
    /**
     * Executa operação com timeout para evitar travamentos
     */
    fun executeWithTimeout(
        timeoutMs: Long = 5000,
        operation: () -> Unit,
        onTimeout: () -> Unit = {}
    ) {
        val job = ioScope.launch {
            operation()
        }
        
        ioScope.launch {
            delay(timeoutMs)
            if (job.isActive) {
                job.cancel()
                withContext(Dispatchers.Main) {
                    onTimeout()
                }
            }
        }
    }
    
    /**
     * Limpa recursos quando não precisar mais
     */
    fun cleanup() {
        ioScope.cancel()
        backgroundExecutor.shutdown()
    }
}