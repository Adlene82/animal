package com.example.petmanager.di

import android.content.Context
import androidx.room.Room
import com.example.petmanager.data.local.AppDatabase
import com.example.petmanager.data.local.dao.AnimalDao
import com.example.petmanager.data.local.dao.ContactDao
import com.example.petmanager.data.local.dao.EventDao
import com.example.petmanager.data.local.dao.FoodItemDao
import com.example.petmanager.data.local.dao.WeightRecordDao
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
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "pet_manager_db" // Consistent with previous AppDatabase setup, but can be confirmed
        ).fallbackToDestructiveMigration().build() // Added fallbackToDestructiveMigration as in AppDatabase
    }

    @Provides
    @Singleton
    fun provideAnimalDao(appDatabase: AppDatabase): AnimalDao {
        return appDatabase.animalDao()
    }

    @Provides
    @Singleton
    fun provideWeightRecordDao(appDatabase: AppDatabase): WeightRecordDao {
        return appDatabase.weightRecordDao()
    }

    @Provides
    @Singleton
    fun provideEventDao(appDatabase: AppDatabase): EventDao {
        return appDatabase.eventDao()
    }

    @Provides
    @Singleton
    fun provideFoodItemDao(appDatabase: AppDatabase): FoodItemDao {
        return appDatabase.foodItemDao()
    }

    @Provides
    @Singleton
    fun provideContactDao(appDatabase: AppDatabase): ContactDao {
        return appDatabase.contactDao()
    }
}
