package com.afilaxy.di

import android.content.Context
import androidx.room.Room
import com.afilaxy.data.database.AfilaxyDatabase
import com.afilaxy.data.database.EmergencyDao
import com.afilaxy.data.database.HelperDao
import com.afilaxy.domain.repository.EmergencyRepository
import com.afilaxy.domain.repository.EmergencyRepositoryImpl
import com.afilaxy.security.SecureXmlUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()
    
    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()
    
    @Provides
    @Singleton
    fun provideAfilaxyDatabase(@ApplicationContext context: Context): AfilaxyDatabase {
        return try {
            Room.databaseBuilder(
                context,
                AfilaxyDatabase::class.java,
                "afilaxy_database"
            ).build()
        } catch (e: Exception) {
            android.util.Log.e("AppModule", "Error creating database", e)
            throw e
        }
    }
    
    @Provides
    fun provideEmergencyDao(database: AfilaxyDatabase): EmergencyDao = database.emergencyDao()
    
    @Provides
    fun provideHelperDao(database: AfilaxyDatabase): HelperDao = database.helperDao()
    
    @Provides
    @Singleton
    fun provideEmergencyRepository(
        firestore: FirebaseFirestore,
        emergencyDao: EmergencyDao,
        helperDao: HelperDao
    ): EmergencyRepository = EmergencyRepositoryImpl()
}