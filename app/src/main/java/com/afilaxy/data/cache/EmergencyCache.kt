package com.afilaxy.data.cache

import com.afilaxy.domain.model.Emergency
import com.afilaxy.domain.model.Helper
import com.afilaxy.domain.model.Location
import java.util.concurrent.ConcurrentHashMap

object EmergencyCache {
    
    private val nearbyHelpersCache = ConcurrentHashMap<String, CacheEntry<List<Helper>>>()
    private val emergencyCache = ConcurrentHashMap<String, Emergency>()
    
    private data class CacheEntry<T>(
        val data: T,
        val timestamp: Long,
        val ttl: Long = 300_000L // 5 minutos
    ) {
        fun isValid(): Boolean = System.currentTimeMillis() - timestamp < ttl
    }
    
    fun cacheNearbyHelpers(location: Location, helpers: List<Helper>) {
        val key = "${location.latitude}_${location.longitude}"
        nearbyHelpersCache[key] = CacheEntry(helpers, System.currentTimeMillis())
    }
    
    fun getCachedHelpers(location: Location): List<Helper>? {
        val key = "${location.latitude}_${location.longitude}"
        val entry = nearbyHelpersCache[key]
        return if (entry?.isValid() == true) entry.data else null
    }
    
    fun cacheEmergency(emergency: Emergency) {
        emergencyCache[emergency.id] = emergency
    }
    
    fun getCachedEmergency(emergencyId: String): Emergency? {
        return emergencyCache[emergencyId]
    }
    
    fun clearExpiredEntries() {
        val iterator = nearbyHelpersCache.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (!entry.value.isValid()) {
                iterator.remove()
            }
        }
    }
}