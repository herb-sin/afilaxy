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
    
    fun logPerformance(operationName: String, duration: Long, success: Boolean) {
        try {
            if (!AuthGuard.requireAuthentication("performance_log")) {
                return
            }
            
            val sanitizedName = sanitizeForLogging(operationName)
            val safeDuration = duration.coerceIn(0, 300_000)
            
            if (sanitizedName.isBlank()) {
                SecureLogger.w("Performance", "Invalid operation name")
                return
            }
            
            when {
                safeDuration > VERY_SLOW_OPERATION_THRESHOLD_MS -> {
                    SecurityUtils.safeLog("Performance", "VERY_SLOW_OP detected", SecurityUtils.LogLevel.WARN)
                }
                safeDuration > SLOW_OPERATION_THRESHOLD_MS -> {
                    SecurityUtils.safeLog("Performance", "SLOW_OP detected", SecurityUtils.LogLevel.WARN)
                }
                else -> {
                    SecurityUtils.safeLog("Performance", "Operation completed", SecurityUtils.LogLevel.INFO)
                }
            }
        } catch (e: Exception) {
            SecureLogger.e("PerformanceMonitor", "Error logging performance", e)
        }
    }
    
    private fun sanitizeForLogging(input: String): String {
        return input
            .replace("\n", "_")
            .replace("\r", "_")
            .replace("\t", "_")
            .replace("\u0000", "_")
            .replace(Regex("[\\p{Cntrl}]"), "_")
            .replace("%n", "_")
            .replace("%s", "_")
            .replace("%d", "_")
            .take(50)
            .filter { it.isLetterOrDigit() || it in "_-. " }
            .ifBlank { "UNKNOWN_OPERATION" }
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
            val currentTime = System.currentTimeMillis()
            val expiredKeys = mutableListOf<String>()
            
            // Single pass to identify expired entries
            cache.entries.removeAll { entry ->
                val isExpired = (currentTime - entry.value.timestamp) > entry.value.ttl
                if (isExpired) expiredKeys.add(entry.key)
                isExpired
            }
            
            if (expiredKeys.isNotEmpty()) {
                SecureLogger.d("CacheManager", "Cleaned up expired entries")
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
        
        // Start periodic cache cleanup with proper lifecycle
        CoroutineScope(Dispatchers.IO).launch {
            try {
                while (true) {
                    kotlinx.coroutines.delay(10 * 60 * 1000L) // 10 minutes
                    CacheManager.cleanupExpiredEntries()
                }
            } catch (e: Exception) {
                SecureLogger.e("PerformanceMonitor", "Cache cleanup error", e)
            }
        }
    }
}