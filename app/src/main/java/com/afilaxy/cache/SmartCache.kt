package com.afilaxy.cache

import java.util.concurrent.ConcurrentHashMap

data class CacheEntry<T>(
    val data: T,
    val timestamp: Long = System.currentTimeMillis()
)

class SmartCache<T> {
    private val cache = ConcurrentHashMap<String, CacheEntry<T>>()
    
    fun get(key: String, maxAge: Long = 5 * 60 * 1000): T? { // 5 minutes default
        val entry = cache[key] ?: return null
        return if (System.currentTimeMillis() - entry.timestamp < maxAge) {
            entry.data
        } else {
            cache.remove(key)
            null
        }
    }
    
    fun put(key: String, data: T) {
        cache[key] = CacheEntry(data)
        
        // Cleanup old entries if cache gets too large
        if (cache.size > 100) {
            cleanupOldEntries()
        }
    }
    
    fun remove(key: String) {
        cache.remove(key)
    }
    
    fun clear() {
        cache.clear()
    }
    
    private fun cleanupOldEntries() {
        val cutoffTime = System.currentTimeMillis() - (30 * 60 * 1000) // 30 minutes
        cache.entries.removeIf { it.value.timestamp < cutoffTime }
    }
    
    companion object {
        private val instances = ConcurrentHashMap<String, SmartCache<*>>()
        
        @Suppress("UNCHECKED_CAST")
        fun <T> getInstance(name: String): SmartCache<T> {
            return instances.getOrPut(name) { SmartCache<T>() } as SmartCache<T>
        }
        
        fun initialize() {
            // Pre-create common caches
            getInstance<Any>("helpers")
            getInstance<Any>("emergencies")
            getInstance<Any>("locations")
        }
    }
}