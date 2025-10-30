package com.afilaxy.testing

import android.content.Context
import android.util.Log
import com.afilaxy.location.RealLocationManager
import com.afilaxy.location.LocationResult

class SimpleEmergencyTest(private val context: Context) {
    
    data class SimpleTestResult(
        val testName: String,
        val success: Boolean,
        val message: String
    )
    
    suspend fun runBasicTests(): List<SimpleTestResult> {
        val results = mutableListOf<SimpleTestResult>()
        
        Log.d("SimpleTest", "🧪 Executando testes básicos")
        
        // Teste 1: Localização
        results.add(testLocation())
        
        // Teste 2: Conectividade
        results.add(testConnectivity())
        
        // Teste 3: Permissões
        results.add(testPermissions())
        
        return results
    }
    
    private suspend fun testLocation(): SimpleTestResult {
        return try {
            val locationManager = RealLocationManager(context)
            when (val result = locationManager.getCurrentLocation()) {
                is LocationResult.Success -> {
                    SimpleTestResult(
                        "📍 GPS e Localização",
                        true,
                        "Localização obtida: ${result.latitude}, ${result.longitude}"
                    )
                }
                is LocationResult.Error -> {
                    SimpleTestResult(
                        "📍 GPS e Localização",
                        false,
                        "Erro GPS: ${result.message}"
                    )
                }
                is LocationResult.PermissionDenied -> {
                    SimpleTestResult(
                        "📍 GPS e Localização",
                        false,
                        "Permissão de localização negada"
                    )
                }
            }
        } catch (e: Exception) {
            SimpleTestResult(
                "📍 GPS e Localização",
                false,
                "Exceção: ${e.message}"
            )
        }
    }
    
    private fun testConnectivity(): SimpleTestResult {
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) 
                as android.net.ConnectivityManager
            val activeNetwork = connectivityManager.activeNetworkInfo
            val isConnected = activeNetwork?.isConnectedOrConnecting == true
            
            SimpleTestResult(
                "🌐 Conectividade",
                isConnected,
                if (isConnected) "Internet disponível" else "Sem conexão com internet"
            )
        } catch (e: Exception) {
            SimpleTestResult(
                "🌐 Conectividade",
                false,
                "Erro ao verificar conectividade: ${e.message}"
            )
        }
    }
    
    private fun testPermissions(): SimpleTestResult {
        return try {
            val hasLocationPermission = android.content.pm.PackageManager.PERMISSION_GRANTED == 
                androidx.core.content.ContextCompat.checkSelfPermission(
                    context, 
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                )
            
            SimpleTestResult(
                "🔐 Permissões",
                hasLocationPermission,
                if (hasLocationPermission) "Permissões necessárias concedidas" 
                else "Permissão de localização não concedida"
            )
        } catch (e: Exception) {
            SimpleTestResult(
                "🔐 Permissões",
                false,
                "Erro ao verificar permissões: ${e.message}"
            )
        }
    }
}