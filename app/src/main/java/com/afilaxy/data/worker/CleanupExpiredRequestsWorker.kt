package com.afilaxy.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.afilaxy.data.repository.EmergencyRequestRepository

class CleanupExpiredRequestsWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    private val emergencyRequestRepository = EmergencyRequestRepository()
    
    override suspend fun doWork(): Result {
        return try {
            android.util.Log.d("CleanupWorker", "Iniciando limpeza de pedidos expirados")
            
            emergencyRequestRepository.cleanupExpiredRequests()
            
            android.util.Log.d("CleanupWorker", "Limpeza concluída com sucesso")
            Result.success()
        } catch (e: Exception) {
            android.util.Log.e("CleanupWorker", "Erro na limpeza: ${e.message}")
            Result.retry()
        }
    }
}