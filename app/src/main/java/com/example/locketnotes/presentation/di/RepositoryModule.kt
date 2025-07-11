package com.example.locketnotes.presentation.di

import com.example.locketnotes.presentation.data.repository.FriendsRepository
import com.example.locketnotes.presentation.data.repository.FriendsRepositoryImpl
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
    abstract fun bindFriendsRepository(
        impl: FriendsRepositoryImpl
    ): FriendsRepository
}