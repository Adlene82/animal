package com.example.petmanager.di

import com.example.petmanager.data.repository.PetRepository
import com.example.petmanager.data.repository.PetRepositoryImpl
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
    abstract fun bindPetRepository(impl: PetRepositoryImpl): PetRepository
}
