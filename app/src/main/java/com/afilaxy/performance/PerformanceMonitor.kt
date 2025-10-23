package com.afilaxy.performance

import com.afilaxy.security.SecureLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.system.measureTimeMillis

/**
 * Performance monitoring utility for Afilaxy
 * Tracks operation performance and identifies bottlenecks
 */
object PerformanceMonitor {
    
    private const val SLOW_OPERATION_THRESHOLD_MS = 1000L
    private const val VERY_SLOW_OPERATION_THRESHOLD_MS = 3000L
    
    /**
     * Measure and log operation performance
     */
    fun <T> measureOperation(
        operationName: String,
        operation: () -> T
    ): T {
        val result: T
        val duration = measureTimeMillis {
            result = operation()
        }
        
        logPerformance(operationName, duration, true)
        return result
    }
    
    /**
     * Measure async operation performance
     */
    suspend fun <T> measureSuspendOperation(
        operationName: String,
        operation: suspend () -> T
    ): T {
        val result: T
        val duration = measureTimeMillis {
            result = operation()
        }
        
        logPerformance(operationName, duration, true)
        return result
    }
    
    /**
     * Measure operation with error handling
     */
    fun <T> measureOperationSafe(
        operationName: String,
        operation: () -> T
    ): T? {
        return try {
            val result: T
            val duration = measureTimeMillis {
                result = operation()
            }
            logPerformance(operationName, duration, true)
            result
        } catch (e: Exception) {
            logPerformance(operationName, 0, false)
            SecureLogger.e("PerformanceMonitor", "Operation failed: $operationName", e)
            null
        }
    }
    
    /**
     * Log performance metrics
     */
    fun logPerformance(operationName: String, duration: Long, success: Boolean) {
        when {
            duration > VERY_SLOW_OPERATION_THRESHOLD_MS -> {
                SecureLogger.w("Performance", "VERY SLOW: $operationName took ${duration}ms")
            }
            duration > SLOW_OPERATION_THRESHOLD_MS -> {
                SecureLogger.w("Performance", "SLOW: $operationName took ${duration}ms")
            }
            else -> {
                SecureLogger.performance(operationName, duration, success)
            }
        }
    }
    
    /**
     * Optimize heavy operations by running on background thread
     */
    suspend fun <T> optimizeHeavyOperation(
        operationName: String,
        operation: suspend () -> T
    ): T {
        return withContext(Dispatchers.IO) {
            measureSuspendOperation(operationName, operation)
        }
    }
    
    /**
     * Cache manager for expensive operations
     */
    object CacheManager {
        val cache = mutableMapOf<String, CacheEntry<*>>()
        const val DEFAULT_TTL_MS = 5 * 60 * 1000L // 5 minutes
        
        data class CacheEntry<T>(
            val value: T,
            val timestamp: Long,
            val ttl: Long
        ) {
            fun isExpired(): Boolean = System.currentTimeMillis() - timestamp > ttl
        }
        
        /**
         * Get cached value or compute if not available/expired
         */
        suspend fun <T> getOrCompute(
            key: String,
            ttlMs: Long = DEFAULT_TTL_MS,
            computation: suspend () -> T
        ): T {
            val cached = cache[key] as? CacheEntry<T>
            
            return if (cached != null && !cached.isExpired()) {
                SecureLogger.d("CacheManager", "Cache hit for: $key")
                cached.value
            } else {
                SecureLogger.d("CacheManager", "Cache miss for: $key")
                val result = optimizeHeavyOperation("compute_$key") { computation() }
                cache[key] = CacheEntry(result, System.currentTimeMillis(), ttlMs)
                result
            }
        }
        
        /**
         * Clear expired cache entries
         */
        fun cleanupExpiredEntries() {
            val expiredKeys = cache.filter { it.value.isExpired() }.keys
            expiredKeys.forEach { cache.remove(it) }
            
            if (expiredKeys.isNotEmpty()) {
                SecureLogger.d("CacheManager", "Cleaned up ${expiredKeys.size} expired entries")
            }
        }
        
        /**
         * Clear all cache entries
         */
        fun clearAll() {
            val size = cache.size
            cache.clear()
            SecureLogger.d("CacheManager", "Cleared $size cache entries")
        }
    }
    
    /**
     * Memory usage monitor
     */
    object MemoryMonitor {
        private const val MEMORY_WARNING_THRESHOLD = 0.8 // 80% of max memory
        
        /**
         * Check memory usage and log warnings
         */
        fun checkMemoryUsage(context: String) {
            val runtime = Runtime.getRuntime()
            val maxMemory = runtime.maxMemory()
            val totalMemory = runtime.totalMemory()
            val freeMemory = runtime.freeMemory()
            val usedMemory = totalMemory - freeMemory
            val memoryUsageRatio = usedMemory.toDouble() / maxMemory.toDouble()
            
            if (memoryUsageRatio > MEMORY_WARNING_THRESHOLD) {
                SecureLogger.w("MemoryMonitor", 
                    "High memory usage in $context: ${(memoryUsageRatio * 100).toInt()}%")
                
                // Suggest garbage collection
                System.gc()
                
                // Clean up cache if memory is critical
                if (memoryUsageRatio > 0.9) {
                    CacheManager.cleanupExpiredEntries()
                }
            }
        }
        
        /**
         * Get memory usage statistics
         */
        fun getMemoryStats(): MemoryStats {
            val runtime = Runtime.getRuntime()
            val maxMemory = runtime.maxMemory()
            val totalMemory = runtime.totalMemory()
            val freeMemory = runtime.freeMemory()
            val usedMemory = totalMemory - freeMemory
            
            return MemoryStats(
                maxMemory = maxMemory,
                totalMemory = totalMemory,
                freeMemory = freeMemory,
                usedMemory = usedMemory,
                usagePercentage = (usedMemory.toDouble() / maxMemory.toDouble() * 100).toInt()
            )
        }
    }
    
    data class MemoryStats(
        val maxMemory: Long,
        val totalMemory: Long,
        val freeMemory: Long,
        val usedMemory: Long,
        val usagePercentage: Int
    )
    
    /**
     * Initialize performance monitoring
     */
    fun initialize() {
        SecureLogger.d("PerformanceMonitor", "Performance monitoring initialized")
        
        // Start periodic cache cleanup
        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                kotlinx.coroutines.delay(10 * 60 * 1000L) // 10 minutes
                CacheManager.cleanupExpiredEntries()
            }
        }
    }
}