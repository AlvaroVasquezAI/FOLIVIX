package com.example.folivix.di

import android.content.Context
import com.example.folivix.data.preferences.AppPreferences
import com.example.folivix.data.repository.*
import com.example.folivix.data.storage.FileStorageManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindAnalysisRepository(
        impl: AnalysisRepositoryImpl
    ): AnalysisRepository

    @Binds
    @Singleton
    abstract fun bindDiseaseInfoRepository(
        impl: DiseaseInfoRepositoryImpl
    ): DiseaseInfoRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        impl: UserRepositoryImpl
    ): UserRepository

    companion object {
        @Provides
        @Singleton
        fun provideFileStorageManager(@ApplicationContext context: Context): FileStorageManager {
            return FileStorageManager(context)
        }

        @Provides
        @Singleton
        fun provideAppPreferences(@ApplicationContext context: Context): AppPreferences {
            return AppPreferences(context)
        }
    }
}