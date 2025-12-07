package com.example.godeye.di

import com.example.godeye.utils.PlateDetector
import com.example.godeye.utils.VideoProcessor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo Hilt para Utilidades
 * Proporciona instancias de servicios de utilidad como OCR y procesamiento de video
 */
@Module
@InstallIn(SingletonComponent::class)
object UtilsModule {

 @Provides
 @Singleton
 fun providePlateDetector(): PlateDetector {
 return PlateDetector
 }

 @Provides
 @Singleton
 fun provideVideoProcessor(): VideoProcessor {
 return VideoProcessor
 }
}

