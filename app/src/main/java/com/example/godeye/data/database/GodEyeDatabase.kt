package com.example.godeye.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
 entities = [CaptureEntity::class, ReportEntity::class, UserProfileEntity::class, HistoryEntity::class],
 version = 5,
 exportSchema = false
)
abstract /**
 * GodEyeDatabase
 *
 * Descripción: [Agregar descripción]
 *
 * @author GodEye Team
 * @version 1.0
 * @since 2025-12-07
 */
class GodEyeDatabase : RoomDatabase() {
 abstract fun captureDao(): CaptureDao
 abstract fun reportDao(): ReportDao
 abstract fun userProfileDao(): UserProfileDao
 abstract fun historyDao(): HistoryDao

 companion object {
 @Volatile
 private var INSTANCE: GodEyeDatabase? = null

 fun getDatabase(context: Context): GodEyeDatabase {
 return INSTANCE ?: synchronized(this) {
 val instance = Room.databaseBuilder(
 context.applicationContext,
 GodEyeDatabase::class.java,
 "godeye_database"
 )
 .fallbackToDestructiveMigration()
 .build()
 INSTANCE = instance
 instance
 }
 }
 }
}

