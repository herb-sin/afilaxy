package com.afilaxy.data.database

import androidx.paging.PagingSource
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface EmergencyDao {
    @Query("SELECT * FROM emergencies WHERE isSynced = 0")
    suspend fun getUnsyncedEmergencies(): List<EmergencyEntity>
    
    @Query("SELECT * FROM emergencies ORDER BY timestamp DESC")
    fun getAllEmergenciesPaged(): PagingSource<Int, EmergencyEntity>
    
    @Query("SELECT * FROM emergencies WHERE userId = :userId ORDER BY timestamp DESC")
    fun getUserEmergencies(userId: String): Flow<List<EmergencyEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmergency(emergency: EmergencyEntity)
    
    @Update
    suspend fun updateEmergency(emergency: EmergencyEntity)
    
    @Query("UPDATE emergencies SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: String)
    
    @Delete
    suspend fun deleteEmergency(emergency: EmergencyEntity)
}

@Dao
interface HelperDao {
    @Query("SELECT * FROM helpers WHERE isAvailable = 1 ORDER BY distanciaMetros ASC")
    suspend fun getAvailableHelpers(): List<HelperEntity>
    
    @Query("SELECT * FROM helpers ORDER BY rating DESC")
    fun getHelpersPaged(): PagingSource<Int, HelperEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHelper(helper: HelperEntity)
    
    @Query("UPDATE helpers SET rating = :rating WHERE id = :id")
    suspend fun updateHelperRating(id: String, rating: Float)
    
    @Query("DELETE FROM helpers WHERE lastSeen < :cutoffTime")
    suspend fun cleanupOldHelpers(cutoffTime: Long)
}