package com.afilaxy.domain.repository

import com.afilaxy.domain.model.Emergency
import com.afilaxy.domain.model.EmergencyStatus
import com.afilaxy.domain.model.Helper
import com.afilaxy.domain.model.Location
import com.afilaxy.security.AuthGuard
import com.afilaxy.security.InputSanitizer
import com.afilaxy.security.RateLimiter
import com.afilaxy.data.cache.EmergencyCache
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.tasks.await
import kotlin.math.*

class EmergencyRepositoryImpl : EmergencyRepository {
    
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    
    override suspend fun createEmergency(location: Location): Emergency {
        val user = AuthGuard.requireVerifiedEmail()
        
        val userDoc = firestore.collection("users").document(user.uid).get().await()
        val userName = userDoc.getString("name") ?: "Pessoa"
        
        val emergency = Emergency(
            id = "",
            userId = user.uid,
            userName = userName,
            location = location,
            status = EmergencyStatus.ACTIVE,
            timestamp = System.currentTimeMillis()
        )

        val emergencyData = mapOf(
            "userId" to emergency.userId,
            "userName" to emergency.userName,
            "location" to GeoPoint(location.latitude, location.longitude),
            "timestamp" to emergency.timestamp,
            "status" to emergency.status.name
        )

        val docRef = firestore.collection("emergencies").add(emergencyData).await()
        val finalEmergency = emergency.copy(id = docRef.id)
        
        // Cache emergência para acesso offline
        EmergencyCache.cacheEmergency(finalEmergency)
        
        return finalEmergency
    }
    
    override suspend fun findNearbyHelpers(location: Location): List<Helper> {
        // Verificar cache primeiro
        EmergencyCache.getCachedHelpers(location)?.let { cachedHelpers ->
            android.util.Log.d("EmergencyRepository", "Usando helpers do cache")
            return cachedHelpers
        }
        
        val currentUserId = AuthGuard.getCurrentUserId()
        
        val usersSnapshot = firestore.collection("users")
            .whereEqualTo("isHelper", true)
            .get()
            .await()

        val helpers = mutableListOf<Helper>()

        for (document in usersSnapshot.documents) {
            if (document.id == currentUserId) continue
            
            val userLocation = document.getGeoPoint("location")
            if (userLocation != null) {
                val distance = calculateDistance(
                    location.latitude, location.longitude,
                    userLocation.latitude, userLocation.longitude
                )

                if (distance <= 0.3) { // 300m radius
                    val userName = document.getString("name")
                    val displayName = if (userName.isNullOrBlank() || userName.contains("@")) {
                        "Ajudante ${helpers.size + 1}"
                    } else {
                        userName
                    }
                    
                    helpers.add(Helper(
                        id = document.id,
                        nome = displayName,
                        distanciaEstimada = "${(distance * 1000).toInt()}m",
                        distanciaMetros = distance * 1000
                    ))
                }
            }
        }

        val sortedHelpers = helpers.sortedBy { it.distanciaMetros }
        
        // Cache resultado para próximas consultas
        EmergencyCache.cacheNearbyHelpers(location, sortedHelpers)
        
        return sortedHelpers
    }
    
    override suspend fun notifyHelpers(helpers: List<Helper>, emergency: Emergency) {
        AuthGuard.requireAuthentication()

        for (helper in helpers) {
            if (!RateLimiter.canSendNotification(emergency.userId)) continue
            
            val alertData = mapOf(
                "type" to "emergency_alert",
                "emergencyId" to emergency.id,
                "requesterName" to InputSanitizer.sanitizeForFirestore(emergency.userName),
                "requesterId" to InputSanitizer.sanitizeForFirestore(emergency.userId),
                "location" to GeoPoint(emergency.location.latitude, emergency.location.longitude),
                "timestamp" to System.currentTimeMillis()
            )
            
            try {
                firestore.collection("users")
                    .document(helper.id)
                    .collection("notifications")
                    .add(alertData)
                    .await()
            } catch (e: Exception) {
                android.util.Log.e("EmergencyRepository", "Erro ao notificar helper")
            }
        }
    }
    
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371.0 // km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadius * c
    }
}