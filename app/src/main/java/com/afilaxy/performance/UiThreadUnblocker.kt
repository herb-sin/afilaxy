package com.afilaxy.performance

import kotlinx.coroutines.*

/**
 * Simple performance helper - removed problematic recursive yielding
 */
object UiThreadUnblocker {
    
    private val backgroundScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    /**
     * Execute operation on background thread
     */
    fun executeInBackground(operation: suspend () -> Unit) {
        backgroundScope.launch {
            operation()
        }
    }
}