package com.roadrelief.app.di

import android.content.Context
import androidx.room.Room
import com.roadrelief.app.data.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "road_relief_db"
        ).build()
    }

    @Provides
    fun provideUserDao(appDatabase: AppDatabase) = appDatabase.userDao()

    @Provides
    fun provideCaseDao(appDatabase: AppDatabase) = appDatabase.caseDao()

    @Provides
    fun provideEvidenceDao(appDatabase: AppDatabase) = appDatabase.evidenceDao()
}
