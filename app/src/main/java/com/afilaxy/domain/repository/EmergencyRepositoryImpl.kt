package com.afilaxy.domain.repository

import com.afilaxy.domain.model.Emergency
import com.afilaxy.domain.model.EmergencyStatus
import com.afilaxy.domain.model.Helper
import com.afilaxy.domain.model.Location
import com.afilaxy.security.AuthGuard
import com.afilaxy.security.InputSanitizer
import com.afilaxy.security.RateLimiter
import com.afilaxy.security.SecurityUtils
import com.afilaxy.security.SqlInjectionPrevention
import com.afilaxy.security.SecureLogger
import com.afilaxy.security.ErrorHandler
import com.afilaxy.data.cache.EmergencyCache
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.tasks.await
import kotlin.math.*

/**
 * Emergency repository implementation with comprehensive security measures
 * 
 * Security features:
 * - SQL/NoSQL injection prevention
 * - Input sanitization and validation
 * - Authentication checks
 * - Rate limiting
 * - Secure error handling
 */
class EmergencyRepositoryImpl : EmergencyRepository {
    
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    
    override suspend fun createEmergency(location: Location): Emergency {
        return try {
            // Authentication check
            val user = FirebaseAuth.getInstance().currentUser
                ?: throw SecurityException("User not authenticated")
            
            // Validate location coordinates
            if (!SecurityUtils.isValidCoordinate(location.latitude, location.longitude)) {
                throw IllegalArgumentException("Invalid coordinates provided")
            }
            
            // Rate limiting check
            if (!RateLimiter.canCreateEmergency(user.uid)) {
                throw SecurityException("Too many emergency requests. Please wait.")
            }
            
            SecureLogger.emergency("Creating emergency request", "coordinates_validated")
            
            val userDoc = firestore.collection("users").document(user.uid).get().await()
            val rawName = userDoc.getString("name")
            
            // Enhanced name sanitization with injection prevention
            val userName = if (rawName != null && SqlInjectionPrevention.isValidSqlInput(rawName)) {
                InputSanitizer.sanitizeName(rawName).takeIf { it.isNotBlank() } ?: "Pessoa"
            } else {
                "Pessoa"
            }
        
        val emergency = Emergency(
            id = "",
            userId = user.uid,
            userName = userName,
            location = location,
            status = EmergencyStatus.ACTIVE,
            timestamp = System.currentTimeMillis()
        )

            // Validate all data before Firestore insertion
            val sanitizedUserId = InputSanitizer.sanitizeForFirestore(emergency.userId)
            val sanitizedUserName = InputSanitizer.sanitizeForFirestore(emergency.userName)
            
            // Using hardcoded field names for security
            
            val emergencyData = mapOf(
                "userId" to sanitizedUserId,
                "userName" to sanitizedUserName,
                "location" to GeoPoint(location.latitude, location.longitude),
                "timestamp" to emergency.timestamp,
                "status" to emergency.status.name
            )

            val docRef = firestore.collection("emergencies").add(emergencyData).await()
            val finalEmergency = emergency.copy(id = docRef.id)
            
            // Cache emergency for offline access
            EmergencyCache.cacheEmergency(finalEmergency)
            
            SecureLogger.userAction("EMERGENCY_CREATED", user.uid, true)
            finalEmergency
            
        } catch (e: SecurityException) {
            val currentUser = FirebaseAuth.getInstance().currentUser
            SecureLogger.security("CREATE_EMERGENCY", "SECURITY_VIOLATION", currentUser?.uid)
            throw e
        } catch (e: IllegalArgumentException) {
            SecureLogger.w("EmergencyRepository", "Invalid emergency data provided")
            throw e
        } catch (e: Exception) {
            val error = ErrorHandler.handleFirebaseError(e, "CREATE_EMERGENCY")
            SecureLogger.e("EmergencyRepository", "Failed to create emergency", e)
            throw Exception("Failed to create emergency request")
        }
    }
    
    override suspend fun findNearbyHelpers(location: Location): List<Helper> {
        return try {
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser == null) {
                SecureLogger.security("HELPER_SEARCH", "UNAUTHENTICATED_ACCESS")
                return emptyList()
            }
            
            // Validate location coordinates
            if (!SecurityUtils.isValidCoordinate(location.latitude, location.longitude)) {
                SecureLogger.w("EmergencyRepository", "Invalid coordinates for helper search")
                return emptyList()
            }
            
            findNearbyHelpersInternal(location, currentUser.uid)
            
        } catch (e: Exception) {
            val error = ErrorHandler.handleException(e, "FIND_HELPERS")
            SecureLogger.e("EmergencyRepository", "Error finding helpers", e)
            emptyList()
        }
    }
    
    private suspend fun findNearbyHelpersInternal(location: Location, currentUserId: String): List<Helper> {
        
        // Check cache first
        EmergencyCache.getCachedHelpers(location)?.let { cachedHelpers ->
            SecureLogger.d("EmergencyRepository", "Using cached helpers")
            return cachedHelpers
        }
        
        val usersSnapshot = firestore.collection("users")
            .whereEqualTo("isHelper", true)
            .get()
            .await()

        val helpers = mutableListOf<Helper>()

        for (document in usersSnapshot.documents) {
            if (document.id == currentUserId) continue
            
            val userLocation = document.getGeoPoint("location")
            if (userLocation != null) {
                // Validate helper location coordinates
                if (!SecurityUtils.isValidCoordinate(userLocation.latitude, userLocation.longitude)) {
                    continue
                }
                
                val distance = calculateDistance(
                    location.latitude, location.longitude,
                    userLocation.latitude, userLocation.longitude
                )

                if (distance <= 0.3) { // 300m radius
                    val rawName = document.getString("name")
                    
                    // Enhanced name validation and sanitization
                    val userName = if (rawName != null && 
                                     SqlInjectionPrevention.isValidSqlInput(rawName) &&
                                     rawName.length <= 50) {
                        InputSanitizer.sanitizeName(rawName)
                    } else {
                        ""
                    }
                    
                    val displayName = if (userName.isBlank() || userName.contains("@")) {
                        "Ajudante ${helpers.size + 1}"
                    } else {
                        userName
                    }
                    
                    // Validate document ID before using
                    val helperId = if (SqlInjectionPrevention.isValidFirebasePath(document.id)) {
                        document.id
                    } else {
                        continue // Skip invalid helper ID
                    }
                    
                    helpers.add(Helper(
                        id = helperId,
                        nome = displayName,
                        distanciaEstimada = "${(distance * 1000).toInt()}m",
                        distanciaMetros = distance * 1000
                    ))
                }
            }
        }

        val sortedHelpers = helpers.sortedBy { it.distanciaMetros }.take(10) // Limit results
        
        // Cache results for future queries
        EmergencyCache.cacheNearbyHelpers(location, sortedHelpers)
        
        SecureLogger.d("EmergencyRepository", "Found ${sortedHelpers.size} nearby helpers")
        return sortedHelpers
    }
    
    override suspend fun notifyHelpers(helpers: List<Helper>, emergency: Emergency) {
        try {
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser == null) {
                SecureLogger.security("NOTIFY_HELPERS", "UNAUTHENTICATED_ACCESS")
                return
            }
            
            // Validate emergency data
            if (!SecurityUtils.isValidCoordinate(emergency.location.latitude, emergency.location.longitude)) {
                SecureLogger.w("EmergencyRepository", "Invalid emergency location for notifications")
                return
            }
            
            notifyHelpersInternal(helpers, emergency)
            
        } catch (e: Exception) {
            val error = ErrorHandler.handleException(e, "NOTIFY_HELPERS")
            SecureLogger.e("EmergencyRepository", "Error notifying helpers", e)
        }
    }
    
    private suspend fun notifyHelpersInternal(helpers: List<Helper>, emergency: Emergency) {

        for (helper in helpers) {
            if (!RateLimiter.canSendNotification(emergency.userId)) continue
            
            // Validate helper ID
            if (!SqlInjectionPrevention.isValidFirebasePath(helper.id)) {
                SecureLogger.w("EmergencyRepository", "Invalid helper ID detected")
                continue
            }
            
            val alertData = mapOf(
                "type" to "emergency_alert",
                "emergencyId" to InputSanitizer.sanitizeForFirestore(emergency.id),
                "requesterName" to InputSanitizer.sanitizeForFirestore(emergency.userName),
                "requesterId" to InputSanitizer.sanitizeForFirestore(emergency.userId),
                "location" to GeoPoint(emergency.location.latitude, emergency.location.longitude),
                "timestamp" to System.currentTimeMillis(),
                "processed" to false
            )
            
            try {
                // Save notification to Firestore
                firestore.collection("users")
                    .document(helper.id)
                    .collection("notifications")
                    .add(alertData)
                    .await()
                
                // Send push notification via Cloud Function
                val pushData = mapOf(
                    "to" to InputSanitizer.sanitizeForFirestore(helper.id),
                    "data" to mapOf(
                        "type" to "emergency_alert",
                        "title" to "🚨 EMERGÊNCIA AFILAXY",
                        "body" to "${InputSanitizer.sanitizeForFirestore(emergency.userName)} precisa de bombinha próximo a você!",
                        "emergencyId" to InputSanitizer.sanitizeForFirestore(emergency.id),
                        "requesterName" to InputSanitizer.sanitizeForFirestore(emergency.userName)
                    )
                )
                
                firestore.collection("push_notifications")
                    .add(pushData)
                    .await()
                
                SecureLogger.d("EmergencyRepository", "Notification sent to helper")
                    
            } catch (e: Exception) {
                val error = ErrorHandler.handleFirebaseError(e, "NOTIFY_HELPER")
                SecureLogger.e("EmergencyRepository", "Failed to notify helper", e)
            }
        }
    }
    
    suspend fun rateHelper(helperId: String, rating: Int, feedback: String) {
        try {
            if (!AuthGuard.isUserAuthenticated()) {
                throw SecurityException("Authentication required")
            }
            
            // CRITICAL: Enhanced validation to prevent SQL injection
            if (!SqlInjectionPrevention.isValidFirebasePath(helperId)) {
                throw IllegalArgumentException("Invalid helper ID")
            }
            
            if (!SqlInjectionPrevention.isValidSqlInput(feedback)) {
                throw IllegalArgumentException("Invalid feedback content")
            }
            
            // CRITICAL: Use parameterized updates to prevent injection
            val sanitizedId = InputSanitizer.preventNoSQLInjection(helperId)
            val sanitizedFeedback = InputSanitizer.preventNoSQLInjection(feedback)
            
            // CRITICAL: Validate sanitized data is not empty after cleaning
            if (sanitizedId.isBlank() || sanitizedFeedback.isBlank()) {
                throw IllegalArgumentException("Invalid data after sanitization")
            }
            
            firestore.collection("helpers")
                .document(sanitizedId)
                .update(mapOf(
                    "rating" to rating.coerceIn(1, 5),
                    "feedback" to sanitizedFeedback
                ))
                .await()
                
            SecureLogger.userAction("RATE_HELPER", AuthGuard.getCurrentUserId() ?: "unknown", true)
            
        } catch (e: SecurityException) {
            SecureLogger.security("RATE_HELPER", "SECURITY_VIOLATION")
            throw e
        } catch (e: IllegalArgumentException) {
            SecureLogger.w("EmergencyRepository", "Invalid rating data provided")
            throw e
        } catch (e: Exception) {
            val error = ErrorHandler.handleFirebaseError(e, "RATE_HELPER")
            SecureLogger.e("EmergencyRepository", "Failed to rate helper", e)
            throw Exception("Failed to submit rating")
        }
    }
    
    /**
     * Calculate distance between two coordinates using Haversine formula
     * @param lat1 Latitude of first point
     * @param lon1 Longitude of first point
     * @param lat2 Latitude of second point
     * @param lon2 Longitude of second point
     * @return Distance in kilometers
     */
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        // Validate coordinates before calculation
        if (!SecurityUtils.isValidCoordinate(lat1, lon1) || 
            !SecurityUtils.isValidCoordinate(lat2, lon2)) {
            return Double.MAX_VALUE // Return max distance for invalid coordinates
        }
        
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