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
import com.afilaxy.security.AuthValidator
import com.afilaxy.security.InputSanitizer

class EmergencyViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(EmergencyUiState())
    val uiState: StateFlow<EmergencyUiState> = _uiState.asStateFlow()
    private var listenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null
    
    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }

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
                // Verificação crítica de autenticação
                val user = try {
                    AuthValidator.requireVerifiedEmail()
                } catch (e: SecurityException) {
                    android.util.Log.e("EmergencyViewModel", "Falha na autenticação")
                    _uiState.value = _uiState.value.copy(
                        locationError = "Usuário deve estar autenticado e verificado",
                        isLoadingLocation = false
                    )
                    return@launch
                }
                
                val emergency = createEmergency(location)

                val helpers = findNearbyHelpers(location)

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

                    // Tentar notificar helpers
                    try {
                        notifyHelpers(helpers, emergency)
                    } catch (e: Exception) {
                        android.util.Log.e("EmergencyViewModel", "Erro crítico ao notificar helpers: ${e.message}")
                        // Manter estado de aguardando resposta mesmo com erro de notificação
                    }
                    
                    // Iniciar listener para respostas dos helpers
                    startListeningForHelperResponse(emergency.id)
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

        // Verificação crítica de autenticação
        val user = AuthValidator.requireVerifiedEmail()

        // Buscar nome do usuário no Firestore
        val userDoc = firestore.collection("users").document(user.uid).get().await()
        val userName = userDoc.getString("name") ?: "Pessoa"
        
        val emergency =
                Emergency(
                        id = "",
                        userId = user.uid,
                        userName = userName,
                        location = location,
                        status = EmergencyStatus.ACTIVE,
                        timestamp = System.currentTimeMillis()
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
        
        android.util.Log.d("EmergencyViewModel", "Buscando helpers próximos")

        val usersSnapshot =
                firestore.collection("users").whereEqualTo("isHelper", true).get().await()
                
        android.util.Log.d("EmergencyViewModel", "Total de helpers encontrados: ${usersSnapshot.documents.size}")

        val helpers = mutableListOf<Helper>()

        for (document in usersSnapshot.documents) {
            android.util.Log.d("EmergencyViewModel", "Verificando helper disponível")
            
            // Excluir o próprio usuário
            if (document.id == currentUserId) {
                android.util.Log.d("EmergencyViewModel", "Pulando próprio usuário")
                continue
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
                
                android.util.Log.d("EmergencyViewModel", "Distância calculada: ${(distance * 1000).toInt()}m")

                if (distance <= 0.3) { // 300m radius
                    val distanciaMetros = distance * 1000
                    val userName = document.getString("name")
                    val displayName = if (userName.isNullOrBlank() || userName.contains("@")) {
                        "Ajudante ${helpers.size + 1}"
                    } else {
                        userName
                    }
                    
                    val helper = Helper(
                            id = document.id,
                            nome = displayName,
                            distanciaEstimada = "${distanciaMetros.toInt()}m",
                            distanciaMetros = distanciaMetros
                    )
                    helpers.add(helper)
                    android.util.Log.d("EmergencyViewModel", "Helper adicionado na lista")
                } else {
                    android.util.Log.d("EmergencyViewModel", "Helper muito distante: ${(distance * 1000).toInt()}m")
                }
            } else {
                android.util.Log.d("EmergencyViewModel", "Helper sem localização salva")
            }
        }
        
        android.util.Log.d("EmergencyViewModel", "Total de helpers próximos: ${helpers.size}")

        return helpers.sortedBy { it.distanciaMetros }
    }


    private suspend fun notifyHelpers(helpers: List<Helper>, emergency: Emergency) {
        // Verificar autenticação antes de operação crítica
        try {
            AuthValidator.requireAuthentication()
        } catch (e: SecurityException) {
            android.util.Log.e("EmergencyViewModel", "Falha na autenticação")
            return
        }
        
        val firestore = FirebaseFirestore.getInstance()
        
        android.util.Log.d("EmergencyViewModel", "Notificando helpers")

        for (helper in helpers) {
            try {
                val alertData = mapOf(
                    "type" to "emergency_alert",
                    "emergencyId" to emergency.id,
                    "requesterName" to emergency.userName.replace("[^\\w\\s-]".toRegex(), ""),
                    "requesterId" to emergency.userId.replace("[^\\w-]".toRegex(), ""),
                    "location" to GeoPoint(
                        emergency.location.latitude,
                        emergency.location.longitude
                    ),
                    "timestamp" to System.currentTimeMillis()
                )
                
                android.util.Log.d("EmergencyViewModel", "Enviando notificação para helper")

                firestore
                    .collection("users")
                    .document(helper.id)
                    .collection("notifications")
                    .add(alertData)
                    .await()
                        
                android.util.Log.d("EmergencyViewModel", "Notificação enviada para helper")
            } catch (e: Exception) {
                android.util.Log.e("EmergencyViewModel", "Erro ao notificar helper")
            }
        }
        
        android.util.Log.d("EmergencyViewModel", "Processo de notificação concluído")
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

    fun startListeningForHelperResponse(emergencyId: String) {
        val currentUser = try {
            AuthValidator.requireAuthentication()
        } catch (e: SecurityException) {
            android.util.Log.w("EmergencyViewModel", "Falha na autenticação")
            return
        }
        
        viewModelScope.launch {
            try {
                val firestore = FirebaseFirestore.getInstance()
                
                // Listener para notificações de confirmação e finalização
                listenerRegistration = firestore.collection("users")
                    .document(currentUser.uid)
                    .collection("notifications")
                    .whereEqualTo("emergencyId", emergencyId)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            android.util.Log.e("EmergencyViewModel", "Erro no listener: ${error.message}")
                            return@addSnapshotListener
                        }
                        
                        snapshot?.documentChanges?.forEach { change ->
                            if (change.type == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                                val doc = change.document
                                val notificationType = doc.getString("type")
                                val helperName = doc.getString("helperName")?.replace("[^\\w\\s-]".toRegex(), "") ?: "Helper"
                                
                                // Usar nome amigável se for email ou vazio
                                val displayName = if (helperName.isBlank() || helperName.contains("@")) {
                                    "Ajudante"
                                } else {
                                    helperName
                                }
                                
                                when (notificationType) {
                                    "helper_responding" -> {
                                        android.util.Log.d("EmergencyViewModel", "Helper respondeu positivamente")
                                        
                                        val helper = Helper(
                                            id = "responding_helper",
                                            nome = displayName,
                                            distanciaEstimada = "A caminho"
                                        )
                                        
                                        _uiState.value = _uiState.value.copy(
                                            helperResponding = helper,
                                            isAwaitingHelperResponse = false
                                        )
                                    }
                                    
                                    "help_completed" -> {
                                        android.util.Log.d("EmergencyViewModel", "Ajuda finalizada pelo helper")
                                        
                                        _uiState.value = _uiState.value.copy(
                                            helperResponding = null,
                                            isAwaitingHelperResponse = false,
                                            helpCompleted = true
                                        )
                                    }
                                }
                                
                                // Marcar como processado
                                doc.reference.update("processed", true)
                            }
                        }
                    }
            } catch (e: Exception) {
                android.util.Log.e("EmergencyViewModel", "Erro ao configurar listener: ${e.message}")
            }
        }
    }

    fun resetEmergencyState() {
        // Verificação de autenticação para operações de estado
        if (!AuthValidator.isUserAuthenticated()) {
            android.util.Log.w("EmergencyViewModel", "Reset de estado sem autenticação")
        }
        
        _uiState.value =
                EmergencyUiState(hasLocationPermission = _uiState.value.hasLocationPermission)
    }
}
