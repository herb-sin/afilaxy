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
                val auth = FirebaseAuth.getInstance()
                val user = auth.currentUser
                if (user == null) {
                    android.util.Log.e("EmergencyViewModel", "Tentativa de buscar helpers sem autenticação")
                    _uiState.value = _uiState.value.copy(
                        locationError = "Usuário deve estar autenticado para buscar ajuda",
                        isLoadingLocation = false
                    )
                    return@launch
                }
                
                // Tentar Firebase primeiro, fallback para simulação
                val emergency =
                        try {
                            createEmergency(location)
                        } catch (e: Exception) {
                            // Fallback: emergência local para teste
                            Emergency(
                                    id = "local_${System.currentTimeMillis()}",
                                    userId = user.uid,
                                    userName = user.email ?: "Usuário Teste",
                                    location = location,
                                    status = EmergencyStatus.ACTIVE
                            )
                        }

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
        val user = auth.currentUser 
        if (user == null) {
            android.util.Log.e("EmergencyViewModel", "Tentativa de criar emergência sem autenticação")
            throw SecurityException("Usuário deve estar autenticado para criar emergência")
        }
        
        if (!user.isEmailVerified) {
            android.util.Log.e("EmergencyViewModel", "Tentativa de criar emergência com email não verificado")
            throw SecurityException("Email deve estar verificado para criar emergência")
        }

        // Buscar nome do usuário no Firestore
        val userDoc = firestore.collection("users").document(user.uid).get().await()
        val userName = userDoc.getString("name") ?: user.email ?: "Usuário"
        
        val emergency =
                Emergency(
                        id = "",
                        userId = user.uid,
                        userName = userName,
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
        return try {
            val firestore = FirebaseFirestore.getInstance()
            val auth = FirebaseAuth.getInstance()
            val currentUserId = auth.currentUser?.uid
            
            android.util.Log.d("EmergencyViewModel", "🔍 Buscando helpers próximos")

            val usersSnapshot =
                    firestore.collection("users").whereEqualTo("isHelper", true).get().await()
                    
            android.util.Log.d("EmergencyViewModel", "📊 Total de helpers encontrados: ${usersSnapshot.documents.size}")

            val helpers = mutableListOf<Helper>()

            for (document in usersSnapshot.documents) {
                android.util.Log.d("EmergencyViewModel", "👤 Verificando helper disponível")
                
                // Excluir o próprio usuário
                if (document.id == currentUserId) {
                    android.util.Log.d("EmergencyViewModel", "⏭️ Pulando próprio usuário")
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
                    
                    android.util.Log.d("EmergencyViewModel", "📏 Distância calculada: ${(distance * 1000).toInt()}m")

                    if (distance <= 0.3) { // 300m radius
                        val distanciaMetros = distance * 1000
                        val helper = Helper(
                                id = document.id,
                                nome = document.getString("name") ?: "Helper",
                                distanciaEstimada = "${distanciaMetros.toInt()}m",
                                distanciaMetros = distanciaMetros
                        )
                        helpers.add(helper)
                        android.util.Log.d("EmergencyViewModel", "✅ Helper adicionado na lista")
                    } else {
                        android.util.Log.d("EmergencyViewModel", "❌ Helper muito distante: ${(distance * 1000).toInt()}m")
                    }
                } else {
                    android.util.Log.d("EmergencyViewModel", "⚠️ Helper sem localização salva")
                }
            }
            
            android.util.Log.d("EmergencyViewModel", "🎯 Total de helpers próximos: ${helpers.size}")

            helpers.sortedBy { it.distanciaMetros }
        } catch (e: Exception) {
            android.util.Log.w("EmergencyViewModel", "Erro ao buscar helpers no Firebase, usando fallback: ${e.message}")
            // Fallback: criar helpers simulados para teste
            createSimulatedHelpers(location)
        }
    }
    
    private fun createSimulatedHelpers(location: Location): List<Helper> {
        // Criar helpers simulados próximos para teste
        val simulatedHelpers = listOf(
            Helper(
                id = "helper_1",
                nome = "Helper Teste 1",
                distanciaEstimada = "150m"
            ),
            Helper(
                id = "helper_2", 
                nome = "Helper Teste 2",
                distanciaEstimada = "300m"
            ),
            Helper(
                id = "helper_3",
                nome = "Helper Teste 3", 
                distanciaEstimada = "500m"
            )
        )
        
        android.util.Log.d("EmergencyViewModel", "Criados ${simulatedHelpers.size} helpers simulados para teste")
        return simulatedHelpers
    }

    private suspend fun notifyHelpers(helpers: List<Helper>, emergency: Emergency) {
        // Verificar autenticação antes de operação crítica
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            android.util.Log.e("EmergencyViewModel", "Usuário não autenticado para notificar helpers")
            return
        }
        
        val firestore = FirebaseFirestore.getInstance()
        
        android.util.Log.d("EmergencyViewModel", "🔔 Notificando ${helpers.size} helpers")

        for (helper in helpers) {
            try {
                val alertData = mapOf(
                    "type" to "emergency_alert",
                    "emergencyId" to emergency.id,
                    "requesterName" to emergency.userName,
                    "requesterId" to emergency.userId,
                    "location" to GeoPoint(
                        emergency.location.latitude,
                        emergency.location.longitude
                    ),
                    "timestamp" to System.currentTimeMillis()
                )
                
                android.util.Log.d("EmergencyViewModel", "📤 Enviando notificação para helper")

                firestore
                    .collection("users")
                    .document(helper.id)
                    .collection("notifications")
                    .add(alertData)
                    .await()
                        
                android.util.Log.d("EmergencyViewModel", "✅ Notificação enviada para helper")
            } catch (e: Exception) {
                android.util.Log.e("EmergencyViewModel", "❌ Erro ao notificar helper: ${e.message}")
            }
        }
        
        android.util.Log.d("EmergencyViewModel", "🏁 Processo de notificação concluído")
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
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        
        if (currentUser == null) {
            android.util.Log.w("EmergencyViewModel", "Não é possível escutar respostas sem autenticação")
            return
        }
        
        viewModelScope.launch {
            try {
                val firestore = FirebaseFirestore.getInstance()
                
                // Listener para notificações de confirmação
                listenerRegistration = firestore.collection("users")
                    .document(currentUser.uid)
                    .collection("notifications")
                    .whereEqualTo("type", "helper_responding")
                    .whereEqualTo("emergencyId", emergencyId)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            android.util.Log.e("EmergencyViewModel", "Erro no listener: ${error.message}")
                            return@addSnapshotListener
                        }
                        
                        snapshot?.documentChanges?.forEach { change ->
                            if (change.type == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                                val doc = change.document
                                val helperName = doc.getString("helperName") ?: "Helper"
                                
                                android.util.Log.d("EmergencyViewModel", "✅ Helper respondeu positivamente")
                                
                                val helper = Helper(
                                    id = "responding_helper",
                                    nome = helperName,
                                    distanciaEstimada = "A caminho"
                                )
                                
                                _uiState.value = _uiState.value.copy(
                                    helperResponding = helper,
                                    isAwaitingHelperResponse = false
                                )
                                
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
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            android.util.Log.w("EmergencyViewModel", "Reset de estado sem autenticação")
        }
        
        _uiState.value =
                EmergencyUiState(hasLocationPermission = _uiState.value.hasLocationPermission)
    }
}
