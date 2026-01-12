package com.afilaxy.di

import com.afilaxy.data.repository.ChatRepository
import com.afilaxy.data.repository.EmergencyRepositoryImpl
import com.afilaxy.data.repository.LocationRepository
import com.afilaxy.domain.repository.EmergencyRepository
import com.afilaxy.domain.repository.IChatRepository
import com.afilaxy.domain.repository.ILocationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindEmergencyRepository(
        emergencyRepositoryImpl: EmergencyRepositoryImpl
    ): EmergencyRepository
    
    @Binds
    @Singleton
    abstract fun bindChatRepository(
        chatRepository: ChatRepository
    ): IChatRepository
    
    @Binds
    @Singleton
    abstract fun bindLocationRepository(
        locationRepository: LocationRepository
    ): ILocationRepository
}
