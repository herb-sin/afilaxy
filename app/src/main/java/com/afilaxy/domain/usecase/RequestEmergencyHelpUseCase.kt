package com.afilaxy.domain.usecase

import com.afilaxy.data.repository.HelperRepository
import com.afilaxy.data.repository.EmergencyRequestRepository
import com.afilaxy.data.NotificationRepository
import com.afilaxy.domain.repository.ILocationRepository
import com.afilaxy.domain.model.Helper
import com.afilaxy.domain.validator.LocationValidator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class RequestEmergencyHelpUseCase(
    private val locationRepository: ILocationRepository,
    private val helperRepository: HelperRepository,
    private val emergencyRequestRepository: EmergencyRequestRepository,
    private val notificationRepository: NotificationRepository
) {
    
    sealed class Result {
        data class Success(val helpers: List<Helper>, val requestId: String) : Result()
        object LocationPermissionRequired : Result()
        object LocationNotAvailable : Result()
        data class LocationInvalid(val reason: String) : Result()
        object NoHelpersFound : Result()
        data class Error(val message: String) : Result()
    }
    
    suspend fun execute(): Result {
        android.util.Log.d("RequestEmergencyHelpUseCase", "Iniciando solicitação de emergência")
        
        // Verificar permissões
        if (!locationRepository.hasLocationPermission()) {
            android.util.Log.w("RequestEmergencyHelpUseCase", "Permissão de localização não concedida")
            return Result.LocationPermissionRequired
        }
        
        // Obter localização
        val location = locationRepository.getCurrentLocation()
        if (location == null) {
            android.util.Log.w("RequestEmergencyHelpUseCase", "Localização não disponível")
            return Result.LocationNotAvailable
        }
        
        android.util.Log.d("RequestEmergencyHelpUseCase", "Localização obtida: (${location.latitude}, ${location.longitude})")
        
        // Validar localização
        when (val validation = LocationValidator.validateLatLng(location.latitude, location.longitude)) {
            is LocationValidator.ValidationResult.Invalid -> {
                android.util.Log.w("RequestEmergencyHelpUseCase", "Localização inválida: ${validation.reason}")
                return Result.LocationInvalid(validation.reason)
            }
            LocationValidator.ValidationResult.Valid -> {
                android.util.Log.d("RequestEmergencyHelpUseCase", "Localização validada com sucesso")
            }
        }
        
        return try {
            // Desativar o usuário como helper (não pode ajudar se está pedindo ajuda)
            android.util.Log.d("RequestEmergencyHelpUseCase", "Desativando usuário como helper (está solicitando ajuda)")
            helperRepository.removeHelper()
            
            // Criar pedido de emergência com timeout
            val requestId = emergencyRequestRepository.createEmergencyRequest(location.latitude, location.longitude)
            if (requestId == null) {
                android.util.Log.e("RequestEmergencyHelpUseCase", "Falha ao criar pedido de emergência")
                return Result.Error("Não foi possível criar o pedido de emergência")
            }
            
            // Buscar helpers próximos (raio de 260 metros)
            android.util.Log.d("RequestEmergencyHelpUseCase", "Buscando helpers próximos...")
            val nearbyHelpers = helperRepository.getNearbyHelpers(
                location.latitude, 
                location.longitude,
                radiusKm = 0.26
            )
            
            android.util.Log.d("RequestEmergencyHelpUseCase", "Encontrados ${nearbyHelpers.size} helpers próximos")
            
            if (nearbyHelpers.isEmpty()) {
                android.util.Log.w("RequestEmergencyHelpUseCase", "Nenhum helper encontrado")
                return Result.NoHelpersFound
            }
            
            // Obter tokens FCM dos helpers
            android.util.Log.d("RequestEmergencyHelpUseCase", "Obtendo tokens FCM dos helpers...")
            val firestore = FirebaseFirestore.getInstance()
            val helperTokens = mutableListOf<String>()
            
            for (helper in nearbyHelpers) {
                try {
                    val userDoc = firestore.collection("users")
                        .document(helper.id)
                        .get()
                        .await()
                    
                    userDoc.getString("fcmToken")?.let { token ->
                        helperTokens.add(token)
                        android.util.Log.d("RequestEmergencyHelpUseCase", "Token FCM obtido para helper: ${helper.name}")
                    }
                } catch (e: Exception) {
                    android.util.Log.e("RequestEmergencyHelpUseCase", "Erro ao obter token FCM para helper ${helper.id}: ${e.message}")
                }
            }
            
            android.util.Log.d("RequestEmergencyHelpUseCase", "Total de tokens FCM: ${helperTokens.size}")
            
            // Enviar notificações
            val auth = FirebaseAuth.getInstance()
            val userName = auth.currentUser?.email?.substringBefore("@") ?: "Usuário"
            val distance = nearbyHelpers.firstOrNull()?.distance?.let { 
                String.format("%.0f", it) 
            } ?: "0"
            
            android.util.Log.d("RequestEmergencyHelpUseCase", "Enviando notificações para $userName (distância: ${distance}m)")
            
            notificationRepository.sendEmergencyNotification(
                helperTokens = helperTokens,
                requesterName = userName,
                distance = distance
            )
            
            android.util.Log.d("RequestEmergencyHelpUseCase", "Solicitação de emergência concluída com sucesso")
            Result.Success(nearbyHelpers, requestId)
            
        } catch (e: Exception) {
            android.util.Log.e("RequestEmergencyHelpUseCase", "Erro na solicitação de emergência: ${e.message}")
            Result.Error(e.message ?: "Erro desconhecido")
        }
    }
}