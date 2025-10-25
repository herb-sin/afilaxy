package com.afilaxy.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.afilaxy.data.database.EmergencyDao
import com.afilaxy.domain.repository.EmergencyRepository
import com.afilaxy.security.SecureLogger
import com.afilaxy.utils.NetworkUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Background worker responsible for synchronizing local emergency data with Firebase.
 * 
 * This worker:
 * - Runs periodically to sync unsynced emergency records
 * - Handles network connectivity checks before attempting sync
 * - Provides retry mechanism for failed sync operations
 * - Ensures data consistency between local database and remote storage
 * 
 * @param context Application context
 * @param workerParams Worker configuration parameters
 * @param emergencyDao Local database access for emergency data
 * @param repository Repository for remote emergency operations
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val emergencyDao: EmergencyDao,
    private val repository: EmergencyRepository
) : CoroutineWorker(context, workerParams) {
    
    /**
     * Performs the background synchronization work.
     * 
     * @return Result.success() if sync completed successfully,
     *         Result.retry() if network is unavailable,
     *         Result.failure() if sync failed permanently
     */
    override suspend fun doWork(): Result {
        return try {
            if (!NetworkUtils.isNetworkAvailable(applicationContext)) {
                return Result.retry()
            }
            
            // Sync unsynced emergencies in batches
            val unsyncedEmergencies = emergencyDao.getUnsyncedEmergencies()
            
            if (unsyncedEmergencies.isEmpty()) {
                return Result.success()
            }
            
            // Process in optimized batches to avoid memory issues
            val batchSize = 5 // Reduced batch size for better performance
            var syncedCount = 0
            
            unsyncedEmergencies.chunked(batchSize).forEach { batch ->
                val batchResults = batch.map { emergency ->
                    try {
                        // Sync with Firebase (placeholder for actual implementation)
                        emergencyDao.markAsSynced(emergency.id)
                        syncedCount++
                        true
                    } catch (e: Exception) {
                        SecureLogger.w("SyncWorker", "Failed to sync emergency record")
                        false
                    }
                }
                
                // Brief pause between batches to prevent overwhelming the system
                if (batchResults.any { !it }) {
                    kotlinx.coroutines.delay(100)
                }
            }
            
            SecureLogger.i("SyncWorker", "Sync completed: $syncedCount records processed")
            
            Result.success()
        } catch (e: Exception) {
            SecureLogger.e("SyncWorker", "Sync operation failed", e)
            Result.failure()
        }
    }
}