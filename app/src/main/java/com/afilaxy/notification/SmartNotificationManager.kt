package com.afilaxy.notification

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmartNotificationManager @Inject constructor() {
    
    fun scheduleLocationReminder(context: Context) {
        val workRequest = PeriodicWorkRequestBuilder<LocationReminderWorker>(
            15, TimeUnit.MINUTES
        ).setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
        ).build()
        
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "location_reminder",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
    
    fun showHighRiskAreaNotification(context: Context) {
        val workRequest = OneTimeWorkRequestBuilder<HighRiskAreaWorker>()
            .setInputData(workDataOf("message" to "Você entrou em uma área de alto risco para asma"))
            .build()
        
        WorkManager.getInstance(context).enqueue(workRequest)
    }
}