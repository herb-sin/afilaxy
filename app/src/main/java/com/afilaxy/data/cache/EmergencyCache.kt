package com.afilaxy.data.cache

import com.afilaxy.domain.model.Emergency
import com.afilaxy.domain.model.Helper
import com.afilaxy.domain.model.Location
import com.afilaxy.security.SecureLogger
import java.util.concurrent.ConcurrentHashMap

/**
 * Emergency data cache for offline functionality
 * Provides cached access to emergency and helper data
 */
object EmergencyCache {
    
    private val emergencyCache = ConcurrentHashMap<String, CacheEntry<Emergency>>()
    private val helperCache = ConcurrentHashMap<String, CacheEntry<List<Helper>>>()
    
    private const val DEFAULT_TTL_MS = 5 * 60 * 1000L // 5 minutes
    private const val HELPER_CACHE_TTL_MS = 2 * 60 * 1000L // 2 minutes for helpers
    
    data class CacheEntry<T>(
        val data: T,
        val timestamp: Long,
        val ttl: Long
    ) {
        fun isExpired(): Boolean = System.currentTimeMillis() - timestamp > ttl
    }
    
    /**
     * Cache emergency data
     */
    fun cacheEmergency(emergency: Emergency, ttlMs: Long = DEFAULT_TTL_MS) {
        try {
            emergencyCache[emergency.id] = CacheEntry(
                data = emergency,
                timestamp = System.currentTimeMillis(),
                ttl = ttlMs
            )
            SecureLogger.d("EmergencyCache", "Emergency cached")
        } catch (e: Exception) {
            SecureLogger.e("EmergencyCache", "Error caching emergency", e)
        }
    }
    
    /**
     * Get cached emergency
     */
    fun getCachedEmergency(emergencyId: String): Emergency? {
        return try {
            val entry = emergencyCache[emergencyId]
            if (entry != null && !entry.isExpired()) {
                SecureLogger.d("EmergencyCache", "Emergency cache hit")
                entry.data
            } else {
                if (entry != null) {
                    emergencyCache.remove(emergencyId)
                    SecureLogger.d("EmergencyCache", "Emergency cache expired")
                }
                null
            }
        } catch (e: Exception) {
            SecureLogger.e("EmergencyCache", "Error getting cached emergency", e)
            null
        }
    }
    
    /**
     * Cache nearby helpers for a location
     */
    fun cacheNearbyHelpers(location: Location, helpers: List<Helper>) {
        try {
            val locationKey = createLocationKey(location)
            helperCache[locationKey] = CacheEntry(
                data = helpers,
                timestamp = System.currentTimeMillis(),
                ttl = HELPER_CACHE_TTL_MS
            )
            SecureLogger.d("EmergencyCache", "Helpers cached for location")
        } catch (e: Exception) {
            SecureLogger.e("EmergencyCache", "Error caching helpers", e)
        }
    }
    
    /**
     * Get cached helpers for a location
     */
    fun getCachedHelpers(location: Location): List<Helper>? {
        return try {
            val locationKey = createLocationKey(location)
            val entry = helperCache[locationKey]
            if (entry != null && !entry.isExpired()) {
                SecureLogger.d("EmergencyCache", "Helper cache hit")
                entry.data
            } else {
                if (entry != null) {
                    helperCache.remove(locationKey)
                    SecureLogger.d("EmergencyCache", "Helper cache expired for location")
                }
                null
            }
        } catch (e: Exception) {
            SecureLogger.e("EmergencyCache", "Error getting cached helpers", e)
            null
        }
    }
    
    /**
     * Create a location-based cache key
     */
    private fun createLocationKey(location: Location): String {
        // Round coordinates to reduce cache fragmentation
        val roundedLat = String.format("%.3f", location.latitude)
        val roundedLon = String.format("%.3f", location.longitude)
        return "${roundedLat}_${roundedLon}"
    }
    
    /**
     * Clear expired cache entries with authentication check
     */
    fun cleanupExpiredEntries() {
        SecurityInterceptor.secureOperation("cache_cleanup") {
            if (emergencyCache.size > 5000 || helperCache.size > 5000) {
                SecurityMonitor.reportSecurityEvent("CACHE_VIOLATION", "Suspicious cache size")
                return@secureOperation
            }
            
            // Ultra-secure iteration with input validation
            val emergencyKeysToRemove = mutableSetOf<String>()
            var processedCount = 0
            
            // Secure controlled iteration - no dynamic code execution
            val emergencyEntries = emergencyCache.entries.toList() // Create safe snapshot
            for (entry in emergencyEntries) {
                if (processedCount >= 500) break
                
                val key = entry.key
                val cacheEntry = entry.value
                
                val validationResult = CentralizedValidator.validateInput(key, CentralizedValidator.InputType.GENERAL)
                if (validationResult.isValid && cacheEntry.isExpired()) {
                    emergencyKeysToRemove.add(key)
                }
                processedCount++
            }
            
            val helperKeysToRemove = mutableSetOf<String>()
            processedCount = 0
            
            // Secure controlled iteration - no dynamic code execution
            val helperEntries = helperCache.entries.toList() // Create safe snapshot
            for (entry in helperEntries) {
                if (processedCount >= 500) break
                
                val key = entry.key
                val cacheEntry = entry.value
                
                val validationResult = CentralizedValidator.validateInput(key, CentralizedValidator.InputType.GENERAL)
                if (validationResult.isValid && cacheEntry.isExpired()) {
                    helperKeysToRemove.add(key)
                }
                processedCount++
            }
            
            // Atomic removal with final validation
            emergencyKeysToRemove.forEach { key ->
                if (emergencyCache.containsKey(key)) {
                    emergencyCache.remove(key)
                }
            }
            
            helperKeysToRemove.forEach { key ->
                if (helperCache.containsKey(key)) {
                    helperCache.remove(key)
                }
            }
            
            if (emergencyKeysToRemove.isNotEmpty() || helperKeysToRemove.isNotEmpty()) {
                SecureLogger.d("EmergencyCache", "Secure cache cleanup completed")
            }
        }
    }
    
    /**
     * Clear all cache entries
     */
    fun clearAll() {
        try {
            emergencyCache.clear()
            helperCache.clear()
            SecureLogger.d("EmergencyCache", "Cleared all cache entries")
        } catch (e: Exception) {
            SecureLogger.e("EmergencyCache", "Error clearing cache", e)
        }
    }
    
    /**
     * Get cache statistics
     */
    fun getCacheStats(): CacheStats {
        return try {
            val emergencyCount = emergencyCache.size
            val helperCount = helperCache.size
            val totalSize = emergencyCount + helperCount
            
            CacheStats(
                emergencyEntries = emergencyCount,
                helperEntries = helperCount,
                totalEntries = totalSize
            )
        } catch (e: Exception) {
            SecureLogger.e("EmergencyCache", "Error getting cache stats", e)
            CacheStats(0, 0, 0)
        }
    }
    
    data class CacheStats(
        val emergencyEntries: Int,
        val helperEntries: Int,
        val totalEntries: Int
    )
}