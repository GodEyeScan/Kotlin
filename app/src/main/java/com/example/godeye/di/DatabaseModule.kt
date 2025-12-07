package com.example.godeye.di

import android.content.Context
import androidx.room.Room
import com.example.godeye.data.database.CaptureDao
import com.example.godeye.data.database.GodEyeDatabase
import com.example.godeye.data.database.ReportDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo Hilt para Database
 * Proporciona instancias de Room Database y DAOs
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

 @Provides
 @Singleton
 fun provideGodEyeDatabase(
 @ApplicationContext context: Context
 ): GodEyeDatabase {
 return Room.databaseBuilder(
 context,
 GodEyeDatabase::class.java,
 "godeye_database"
 )
 .fallbackToDestructiveMigration()
 .build()
 }

 @Provides
 @Singleton
 fun provideCaptureDao(database: GodEyeDatabase): CaptureDao {
 return database.captureDao()
 }

 @Provides
 @Singleton
 fun provideReportDao(database: GodEyeDatabase): ReportDao {
 return database.reportDao()
 }
}

