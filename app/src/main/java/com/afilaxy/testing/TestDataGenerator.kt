package com.afilaxy.testing

import com.afilaxy.domain.model.Emergency
import com.afilaxy.domain.model.EmergencyStatus
import com.afilaxy.domain.model.Helper
import com.afilaxy.domain.model.Location

object TestDataGenerator {
    
    fun generateEmergencies(count: Int, baseLocation: String = "São Paulo, SP"): List<Emergency> {
        val baseCoords = getBaseCoordinates(baseLocation)
        
        return (0 until count).map { index ->
            Emergency(
                id = "test_emergency_$index",
                userId = "test_user_sp_${String.format("%03d", index + 1)}",
                userName = getTestUserName(index),
                location = Location(
                    latitude = baseCoords.first + (Math.random() - 0.5) * 0.01,
                    longitude = baseCoords.second + (Math.random() - 0.5) * 0.01
                ),
                status = EmergencyStatus.ACTIVE,
                timestamp = System.currentTimeMillis() + (index * 60000L)
            )
        }
    }
    
    fun generateHelpers(count: Int, baseLocation: String = "São Paulo, SP"): List<Helper> {
        val baseCoords = getBaseCoordinates(baseLocation)
        
        return (0 until count).map { index ->
            Helper(
                id = "helper_sp_${String.format("%03d", index + 1)}",
                nome = getTestHelperName(index),
                distanciaEstimada = "${(50..300).random()}m",
                distanciaMetros = (50..300).random().toDouble()
            )
        }
    }
    
    private fun getBaseCoordinates(location: String): Pair<Double, Double> {
        return when (location.lowercase()) {
            "são paulo, sp" -> Pair(-23.5505, -46.6333)
            "rio de janeiro, rj" -> Pair(-22.9068, -43.1729)
            "belo horizonte, mg" -> Pair(-19.9167, -43.9345)
            else -> Pair(-23.5505, -46.6333) // Default São Paulo
        }
    }
    
    private fun getTestUserName(index: Int): String {
        val names = listOf(
            "Maria Silva", "João Santos", "Ana Costa", "Carlos Lima", 
            "Lucia Oliveira", "Pedro Alves", "Julia Ferreira", "Roberto Souza"
        )
        return names[index % names.size]
    }
    
    private fun getTestHelperName(index: Int): String {
        val helpers = listOf(
            "Dr. Roberto Medeiros", "Farmácia Central", "Paula Enfermeira",
            "UBS Vila Madalena", "Drogaria São Paulo", "Enf. Carlos Silva"
        )
        return helpers[index % helpers.size]
    }
}