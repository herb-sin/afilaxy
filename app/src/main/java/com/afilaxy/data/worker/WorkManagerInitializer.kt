package com.afilaxy.data.worker

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

object WorkManagerInitializer {
    
    fun scheduleCleanupWork(context: Context) {
        val cleanupRequest = PeriodicWorkRequestBuilder<CleanupExpiredRequestsWorker>(
            1, TimeUnit.HOURS // Executa a cada 1 hora
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()
        
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "cleanup_expired_requests",
            ExistingPeriodicWorkPolicy.KEEP,
            cleanupRequest
        )
        
        android.util.Log.d("WorkManagerInitializer", "Limpeza automática agendada para executar a cada hora")
    }
}