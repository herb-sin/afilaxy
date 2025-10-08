package com.afilaxy.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context

@Database(
    entities = [EmergencyEntity::class, HelperEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AfilaxyDatabase : RoomDatabase() {
    abstract fun emergencyDao(): EmergencyDao
    abstract fun helperDao(): HelperDao
    
    companion object {
        @Volatile
        private var INSTANCE: AfilaxyDatabase? = null
        
        fun getDatabase(context: Context): AfilaxyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AfilaxyDatabase::class.java,
                    "afilaxy_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}