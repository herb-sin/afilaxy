package com.afilaxy.performance

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.util.concurrent.ConcurrentHashMap

object PerformanceManager {
    
    private val cache = ConcurrentHashMap<String, Any>()
    private val backgroundScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    fun <T> lazyLoad(key: String, loader: suspend () -> T): Flow<T> = flow {
        val cached = cache[key] as? T
        if (cached != null) {
            emit(cached)
        } else {
            val result = loader()
            cache[key] = result as Any
            emit(result)
        }
    }.flowOn(Dispatchers.IO)
    
    fun executeInBackground(task: suspend () -> Unit) {
        backgroundScope.launch { task() }
    }
    
    fun clearCache() = cache.clear()
    
    suspend fun <T, R> batchProcess(
        items: List<T>,
        batchSize: Int = 50,
        processor: suspend (List<T>) -> List<R>
    ): List<R> = withContext(Dispatchers.IO) {
        items.chunked(batchSize).flatMap { batch ->
            processor(batch)
        }
    }
}