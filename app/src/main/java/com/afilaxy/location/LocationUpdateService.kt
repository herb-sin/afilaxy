package com.afilaxy.location

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.Looper
import com.afilaxy.data.repository.HelperRepository
import com.google.android.gms.location.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LocationUpdateService : Service() {
    
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private val helperRepository = HelperRepository()
    private val serviceScope = CoroutineScope(Dispatchers.IO)
    
    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        setupLocationCallback()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startLocationUpdates()
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    private fun setupLocationCallback() {
        // Implementação simplificada sem callback
    }
    
    private fun startLocationUpdates() {
        // Implementação simplificada
        android.util.Log.d("LocationService", "Serviço iniciado")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        android.util.Log.d("LocationService", "Serviço finalizado")
    }
}