package com.afilaxy.presentation.emergency

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afilaxy.domain.model.Emergency
import com.afilaxy.domain.model.EmergencyStatus
import com.afilaxy.domain.model.Helper
import com.afilaxy.domain.model.Location
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlin.math.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class EmergencyViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(EmergencyUiState())
    val uiState: StateFlow<EmergencyUiState> = _uiState.asStateFlow()

    fun updateLocationPermission(hasPermission: Boolean) {
        _uiState.value = _uiState.value.copy(hasLocationPermission = hasPermission)
    }

    fun startLocationSearch() {
        _uiState.value =
                _uiState.value.copy(
                        isLoadingLocation = true,
                        locationError = null,
                        userLocation = null,
                        nearbyHelpers = emptyList(),
                        noHelpersFound = false,
                        isAwaitingHelperResponse = false,
                        helperResponding = null
                )
    }

    fun setLocation(location: Location?) {
        if (location != null) {
            _uiState.value = _uiState.value.copy(userLocation = location, isLoadingLocation = false)
            searchNearbyHelpers(location)
        } else {
            _uiState.value =
                    _uiState.value.copy(
                            locationError =
                                    "Não foi possível obter sua localização. Tente novamente.",
                            isLoadingLocation = false
                    )
        }
    }

    fun setLocationError(error: String) {
        _uiState.value = _uiState.value.copy(locationError = error, isLoadingLocation = false)
    }

    private fun searchNearbyHelpers(location: Location) {
        viewModelScope.launch {
            try {
                // Tentar Firebase primeiro, fallback para simulação
                val emergency =
                        try {
                            createEmergency(location)
                        } catch (e: Exception) {
                            // Fallback: emergência local para teste
                            Emergency(
                                    id = "local_${System.currentTimeMillis()}",
                                    userId = "test_user",
                                    userName = "Usuário Teste",
                                    location = location,
                                    status = EmergencyStatus.ACTIVE
                            )
                        }

                val helpers =
                        try {
                            findNearbyHelpers(location)
                        } catch (e: Exception) {
                            // Fallback: helpers simulados para teste
                            listOf(
                                    Helper("1", "Ana Silva", "150m"),
                                    Helper("2", "João Santos", "300m"),
                                    Helper("3", "Maria Costa", "500m")
                            )
                        }

                if (helpers.isEmpty()) {
                    _uiState.value =
                            _uiState.value.copy(
                                    nearbyHelpers = emptyList(),
                                    noHelpersFound = true,
                                    isAwaitingHelperResponse = false,
                                    emergencyId = emergency.id
                            )
                } else {
                    _uiState.value =
                            _uiState.value.copy(
                                    nearbyHelpers = helpers,
                                    noHelpersFound = false,
                                    isAwaitingHelperResponse = true,
                                    helperResponding = null,
                                    emergencyId = emergency.id
                            )

                    // Tentar notificar helpers (pode falhar se Firestore não estiver ativo)
                    try {
                        notifyHelpers(helpers, emergency)
                    } catch (e: Exception) {
                        // Ignorar erro de notificação para teste
                    }
                }
            } catch (e: Exception) {
                _uiState.value =
                        _uiState.value.copy(
                                locationError = "Erro: ${e.message}",
                                isLoadingLocation = false
                        )
            }
        }
    }

    private suspend fun createEmergency(location: Location): Emergency {
        val auth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()

        val user = auth.currentUser ?: throw Exception("Usuário não autenticado")

        val emergency =
                Emergency(
                        id = "",
                        userId = user.uid,
                        userName = user.email ?: "Usuário",
                        location = location,
                        status = EmergencyStatus.ACTIVE
                )

        val emergencyData =
                mapOf(
                        "userId" to emergency.userId,
                        "userName" to emergency.userName,
                        "location" to GeoPoint(location.latitude, location.longitude),
                        "timestamp" to emergency.timestamp,
                        "status" to emergency.status.name
                )

        val docRef = firestore.collection("emergencies").add(emergencyData).await()
        return emergency.copy(id = docRef.id)
    }

    private suspend fun findNearbyHelpers(location: Location): List<Helper> {
        val firestore = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        val currentUserId = auth.currentUser?.uid

        val usersSnapshot =
                firestore.collection("users").whereEqualTo("isHelper", true).get().await()

        val helpers = mutableListOf<Helper>()

        for (document in usersSnapshot.documents) {
            // ✅ CORREÇÃO: Excluir o próprio usuário
            if (document.id == currentUserId) {
                continue // Pula o próprio usuário
            }
            val userLocation = document.getGeoPoint("location")
            if (userLocation != null) {
                val distance =
                        calculateDistance(
                                location.latitude,
                                location.longitude,
                                userLocation.latitude,
                                userLocation.longitude
                        )

                if (distance <= 0.5) { // 500m radius
                    helpers.add(
                            Helper(
                                    id = document.id,
                                    nome = document.getString("name") ?: "Helper",
                                    distanciaEstimada = "${(distance * 1000).toInt()}m"
                            )
                    )
                }
            }
        }

        return helpers.sortedBy { it.distanciaEstimada }
    }

    private suspend fun notifyHelpers(helpers: List<Helper>, emergency: Emergency) {
        val firestore = FirebaseFirestore.getInstance()

        for (helper in helpers) {
            val alertData =
                    mapOf(
                            "type" to "emergency_alert",
                            "emergencyId" to emergency.id,
                            "requesterName" to emergency.userName,
                            "location" to
                                    GeoPoint(
                                            emergency.location.latitude,
                                            emergency.location.longitude
                                    ),
                            "timestamp" to System.currentTimeMillis()
                    )

            firestore
                    .collection("users")
                    .document(helper.id)
                    .collection("notifications")
                    .add(alertData)
                    .await()
        }
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371.0 // km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a =
                sin(dLat / 2) * sin(dLat / 2) +
                        cos(Math.toRadians(lat1)) *
                                cos(Math.toRadians(lat2)) *
                                sin(dLon / 2) *
                                sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadius * c
    }

    fun showEmergencyInstructions() {
        _uiState.value = _uiState.value.copy(showEmergencyInstructions = true)
    }

    fun hideEmergencyInstructions() {
        _uiState.value = _uiState.value.copy(showEmergencyInstructions = false)
    }

    fun resetEmergencyState() {
        _uiState.value =
                EmergencyUiState(hasLocationPermission = _uiState.value.hasLocationPermission)
    }
}
