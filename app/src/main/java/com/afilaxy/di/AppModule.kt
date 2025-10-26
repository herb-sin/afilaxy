package com.afilaxy.di

import android.content.Context
import androidx.room.Room
import com.afilaxy.data.database.AfilaxyDatabase
import com.afilaxy.data.database.EmergencyDao
import com.afilaxy.data.database.HelperDao
import com.afilaxy.domain.repository.EmergencyRepository
import com.afilaxy.domain.repository.EmergencyRepositoryImpl

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
    fun provideFirebaseAuth(): FirebaseAuth {
        return try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            com.afilaxy.security.SecureLogger.e("AppModule", "Error initializing Firebase Auth", e)
            throw SecurityException("Failed to initialize Firebase Auth")
        }
    }
    
    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            com.afilaxy.security.SecureLogger.e("AppModule", "Error initializing Firestore", e)
            throw SecurityException("Failed to initialize Firestore")
        }
    }
    
    @Provides
    @Singleton
    fun provideAfilaxyDatabase(@ApplicationContext context: Context): AfilaxyDatabase {
        return try {
            Room.databaseBuilder(
                context,
                AfilaxyDatabase::class.java,
                "afilaxy_database"
            )
            .fallbackToDestructiveMigration()
            .build()
        } catch (e: Exception) {
            com.afilaxy.security.SecureLogger.e("AppModule", "Error creating database", e)
            throw SecurityException("Failed to create database")
        }
    }
    
    @Provides
    fun provideEmergencyDao(database: AfilaxyDatabase): EmergencyDao {
        return try {
            database.emergencyDao()
        } catch (e: Exception) {
            com.afilaxy.security.SecureLogger.e("AppModule", "Error providing EmergencyDao", e)
            throw SecurityException("Failed to provide EmergencyDao")
        }
    }
    
    @Provides
    fun provideHelperDao(database: AfilaxyDatabase): HelperDao {
        return try {
            database.helperDao()
        } catch (e: Exception) {
            com.afilaxy.security.SecureLogger.e("AppModule", "Error providing HelperDao", e)
            throw SecurityException("Failed to provide HelperDao")
        }
    }
    
    @Provides
    @Singleton
    fun provideEmergencyRepository(): EmergencyRepository {
        return try {
            EmergencyRepositoryImpl()
        } catch (e: Exception) {
            com.afilaxy.security.SecureLogger.e("AppModule", "Error creating repository", e)
            throw SecurityException("Failed to create EmergencyRepository")
        }
    }
    
    @Provides
    @Singleton
    fun provideSecurityValidator(): com.afilaxy.security.SecurityValidator {
        return com.afilaxy.security.SecurityValidator
    }
    
    @Provides
    @Singleton
    fun provideRateLimiter(): com.afilaxy.security.RateLimiter {
        return com.afilaxy.security.RateLimiter
    }
}