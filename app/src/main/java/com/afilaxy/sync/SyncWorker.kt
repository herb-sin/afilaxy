package com.afilaxy.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.afilaxy.data.database.EmergencyDao
import com.afilaxy.domain.repository.EmergencyRepository
import com.afilaxy.utils.NetworkUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val emergencyDao: EmergencyDao,
    private val repository: EmergencyRepository
) : CoroutineWorker(context, workerParams) {
    
    override suspend fun doWork(): Result {
        return try {
            if (!NetworkUtils.isNetworkAvailable(applicationContext)) {
                return Result.retry()
            }
            
            // Sync unsynced emergencies
            val unsyncedEmergencies = emergencyDao.getUnsyncedEmergencies()
            
            unsyncedEmergencies.forEach { emergency ->
                try {
                    // Sync with Firebase
                    // Mark as synced
                    emergencyDao.markAsSynced(emergency.id)
                } catch (e: Exception) {
                    // Log error but continue with other items
                }
            }
            
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}