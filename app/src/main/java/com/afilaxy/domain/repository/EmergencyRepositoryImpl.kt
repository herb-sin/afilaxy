package com.afilaxy.domain.repository

import com.afilaxy.domain.model.Emergency
import com.afilaxy.domain.model.EmergencyStatus
import com.afilaxy.domain.model.Helper
import com.afilaxy.domain.model.Location
import com.afilaxy.security.AuthGuard
import com.afilaxy.security.InputSanitizer
import com.afilaxy.security.RateLimiter
import com.afilaxy.security.SecurityUtils
import com.afilaxy.data.cache.EmergencyCache
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.tasks.await
import kotlin.math.*

class EmergencyRepositoryImpl : EmergencyRepository {
    
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    
    override suspend fun createEmergency(location: Location): Emergency {
        val user = FirebaseAuth.getInstance().currentUser
            ?: throw SecurityException("User not authenticated")
        
        return try {
            val userDoc = firestore.collection("users").document(user.uid).get().await()
            val userName = InputSanitizer.sanitizeName(userDoc.getString("name")) ?: "Pessoa"
        
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
            
            finalEmergency
        } catch (e: SecurityException) {
            throw e
        } catch (e: Exception) {
            SecurityUtils.safeLog("EmergencyRepository", "Failed to create emergency: ${e.message}", SecurityUtils.LogLevel.ERROR)
            throw Exception("Failed to create emergency request")
        }
    }
    
    override suspend fun findNearbyHelpers(location: Location): List<Helper> {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            SecurityUtils.safeLog("EmergencyRepository", "Helper search attempted without authentication", SecurityUtils.LogLevel.WARN)
            return emptyList()
        }
        
        return try {
            // Verificar cache primeiro
            EmergencyCache.getCachedHelpers(location)?.let { cachedHelpers ->
                SecurityUtils.safeLog("EmergencyRepository", "Using cached helpers", SecurityUtils.LogLevel.DEBUG)
                return cachedHelpers
            }
            
            val currentUserId = currentUser.uid
        
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

            val sortedHelpers = helpers.sortedBy { it.distanciaMetros }.take(10) // Limit results
            
            // Cache resultado para próximas consultas
            EmergencyCache.cacheNearbyHelpers(location, sortedHelpers)
            
            sortedHelpers
        } catch (e: Exception) {
            SecurityUtils.safeLog("EmergencyRepository", "Failed to find helpers: ${e.message}", SecurityUtils.LogLevel.ERROR)
            emptyList()
        }
    }
    
    override suspend fun notifyHelpers(helpers: List<Helper>, emergency: Emergency) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            SecurityUtils.safeLog("EmergencyRepository", "Notification send attempted without authentication", SecurityUtils.LogLevel.WARN)
            return
        }

        for (helper in helpers) {
            if (!RateLimiter.canSendNotification(emergency.userId)) continue
            
            val alertData = mapOf(
                "type" to "emergency_alert",
                "emergencyId" to emergency.id,
                "requesterName" to InputSanitizer.sanitizeForFirestore(emergency.userName),
                "requesterId" to InputSanitizer.sanitizeForFirestore(emergency.userId),
                "location" to GeoPoint(emergency.location.latitude, emergency.location.longitude),
                "timestamp" to System.currentTimeMillis(),
                "processed" to false
            )
            
            try {
                // Salvar notificação no Firestore
                firestore.collection("users")
                    .document(helper.id)
                    .collection("notifications")
                    .add(alertData)
                    .await()
                
                // Enviar notificação push via Cloud Function
                val pushData = mapOf(
                    "to" to helper.id,
                    "data" to mapOf(
                        "type" to "emergency_alert",
                        "title" to "🚨 EMERGÊNCIA AFILAXY",
                        "body" to "${emergency.userName} precisa de bombinha próximo a você!",
                        "emergencyId" to emergency.id,
                        "requesterName" to emergency.userName
                    )
                )
                
                firestore.collection("push_notifications")
                    .add(pushData)
                    .await()
                    
            } catch (e: Exception) {
                SecurityUtils.safeLog("EmergencyRepository", "Failed to notify helper ${helper.id}: ${e.message}", SecurityUtils.LogLevel.ERROR)
            }
        }
    }
    
    suspend fun rateHelper(helperId: String, rating: Int, feedback: String) {
        if (!AuthGuard.isUserAuthenticated()) {
            throw SecurityException("Authentication required")
        }
        
        val sanitizedId = InputSanitizer.sanitizeForFirestore(helperId)
        val sanitizedFeedback = InputSanitizer.sanitizeText(feedback)
        
        try {
            firestore.collection("helpers")
                .document(sanitizedId)
                .update(mapOf(
                    "rating" to rating.coerceIn(1, 5),
                    "feedback" to sanitizedFeedback
                ))
                .await()
        } catch (e: Exception) {
            SecurityUtils.safeLog("EmergencyRepository", "Failed to rate helper", SecurityUtils.LogLevel.ERROR)
            throw Exception("Failed to submit rating")
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