package com.afilaxy.domain.repository

import com.afilaxy.domain.model.Emergency
import com.afilaxy.domain.model.Helper
import com.afilaxy.domain.model.Location
import com.afilaxy.security.AuthGuard
import com.afilaxy.security.InputSanitizer
import com.afilaxy.security.SecureLogger
import com.afilaxy.security.SecurityUtils
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmergencyRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : EmergencyRepository {

    override suspend fun createEmergency(emergency: Emergency): Result<String> {
        return try {
            // Enhanced authentication check
            if (!AuthGuard.isUserAuthenticated()) {
                SecureLogger.security("EMERGENCY_CREATE", "UNAUTHORIZED_ACCESS_ATTEMPT")
                return Result.failure(SecurityException("Authentication required"))
            }

            // Enhanced input sanitization
            val sanitizedEmergency = emergency.copy(
                description = InputSanitizer.sanitizeText(emergency.description),
                userName = InputSanitizer.sanitizeName(emergency.userName),
                location = emergency.location.copy(
                    address = InputSanitizer.sanitizeText(emergency.location.address)
                )
            )
            
            // Coordinate validation
            if (!SecurityUtils.isValidCoordinate(
                sanitizedEmergency.location.latitude, 
                sanitizedEmergency.location.longitude
            )) {
                SecureLogger.security("EMERGENCY_CREATE", "INVALID_COORDINATES")
                return Result.failure(IllegalArgumentException("Invalid coordinates"))
            }

            val docRef = firestore.collection("emergencies").document()
            docRef.set(sanitizedEmergency).await()
            
            SecureLogger.d("EmergencyRepository", "Emergency created successfully with ID: ${docRef.id}")
            Result.success(docRef.id)
        } catch (e: Exception) {
            SecureLogger.e("EmergencyRepository", "Failed to create emergency", e)
            Result.failure(e)
        }
    }

    override suspend fun findNearbyHelpers(location: Location, radiusKm: Double): Result<List<Helper>> {
        return try {
            if (!AuthGuard.requireAuthentication("find_helpers")) {
                return Result.failure(SecurityException("Authentication required"))
            }

            val sanitizedCoords = InputSanitizer.sanitizeCoordinates(location.latitude, location.longitude)
                ?: return Result.failure(IllegalArgumentException("Invalid coordinates"))

            val helpers = firestore.collection("helpers")
                .whereEqualTo("available", true)
                .limit(50)
                .get()
                .await()
                .documents
                .mapNotNull { doc ->
                    try {
                        doc.toObject(Helper::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        SecureLogger.w("EmergencyRepository", "Failed to parse helper document")
                        null
                    }
                }
                .filter { helper ->
                    // Calcular distância se helper tiver localização válida
                    true // Simplificado por enquanto
                }

            Result.success(helpers)
        } catch (e: Exception) {
            SecureLogger.e("EmergencyRepository", "Failed to find helpers", e)
            Result.failure(e)
        }
    }

    override suspend fun updateEmergencyStatus(emergencyId: String, status: String): Result<Unit> {
        return try {
            if (!AuthGuard.requireAuthentication("update_emergency")) {
                return Result.failure(SecurityException("Authentication required"))
            }

            val sanitizedId = InputSanitizer.sanitizeText(emergencyId)
            val sanitizedStatus = InputSanitizer.sanitizeText(status)

            if (sanitizedId.isBlank() || sanitizedStatus.isBlank()) {
                return Result.failure(IllegalArgumentException("Invalid parameters"))
            }

            firestore.collection("emergencies")
                .document(sanitizedId)
                .update("status", sanitizedStatus)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            SecureLogger.e("EmergencyRepository", "Failed to update emergency status", e)
            Result.failure(e)
        }
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371.0 // km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = kotlin.math.sin(dLat / 2) * kotlin.math.sin(dLat / 2) +
                kotlin.math.cos(Math.toRadians(lat1)) * kotlin.math.cos(Math.toRadians(lat2)) *
                kotlin.math.sin(dLon / 2) * kotlin.math.sin(dLon / 2)
        val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
        return earthRadius * c
    }
}