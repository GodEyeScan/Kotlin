package com.example.godeye.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CaptureDao {
 @Query("SELECT * FROM captures ORDER BY timestamp DESC")
 fun getAllCaptures(): Flow<List<CaptureEntity>>

 @Query("SELECT * FROM captures ORDER BY timestamp DESC")
 suspend fun getAllCapturesOnce(): List<CaptureEntity>

 @Query("SELECT * FROM captures WHERE userEmail = :email ORDER BY timestamp DESC")
 fun getCapturesByUser(email: String): Flow<List<CaptureEntity>>

 @Query("SELECT * FROM captures WHERE userEmail = :email ORDER BY timestamp DESC")
 suspend fun getCapturesByUserOnce(email: String): List<CaptureEntity>

 @Insert(onConflict = OnConflictStrategy.REPLACE)
 suspend fun insert(capture: CaptureEntity): Long

 @Update
 suspend fun update(capture: CaptureEntity)

 @Delete
 suspend fun delete(capture: CaptureEntity)

 @Query("DELETE FROM captures")
 suspend fun deleteAll()

 @Query("SELECT * FROM captures WHERE id = :id")
 suspend fun getCaptureById(id: Long): CaptureEntity?

 @Query("SELECT * FROM captures WHERE detectedPlate LIKE '%' || :plate || '%' ORDER BY timestamp DESC")
 fun getCapturesByPlate(plate: String): Flow<List<CaptureEntity>>

 @Query("SELECT * FROM captures WHERE detectedPlate LIKE '%' || :plate || '%' AND userEmail = :email ORDER BY timestamp DESC")
 fun getCapturesByPlateAndUser(plate: String, email: String): Flow<List<CaptureEntity>>

 @Query("SELECT COUNT(*) FROM captures")
 suspend fun getCaptureCount(): Int
}

@Dao
interface ReportDao {
 @Query("SELECT * FROM reports ORDER BY timestamp DESC")
 fun getAllReports(): Flow<List<ReportEntity>>

 @Insert(onConflict = OnConflictStrategy.REPLACE)
 suspend fun insert(report: ReportEntity): Long

 @Delete
 suspend fun delete(report: ReportEntity)

 @Query("DELETE FROM reports")
 suspend fun deleteAll()

 @Query("SELECT * FROM reports WHERE userEmail = :email ORDER BY timestamp DESC")
 fun getReportsByUser(email: String): Flow<List<ReportEntity>>
}

/**
 * DAO para el perfil del usuario (datos locales)
 */
@Dao
interface UserProfileDao {
 @Query("SELECT * FROM user_profile WHERE email = :email LIMIT 1")
 suspend fun getProfile(email: String): UserProfileEntity?

 @Insert(onConflict = OnConflictStrategy.REPLACE)
 suspend fun insertOrUpdate(profile: UserProfileEntity)

 @Query("DELETE FROM user_profile WHERE email = :email")
 suspend fun delete(email: String)
}

/**
 * DAO para el historial de fotos (datos locales)
 */
@Dao
interface HistoryDao {
 @Query("SELECT * FROM history WHERE userEmail = :email ORDER BY timestamp DESC")
 fun getHistoryByUser(email: String): Flow<List<HistoryEntity>>

 @Query("SELECT * FROM history WHERE userEmail = :email ORDER BY timestamp DESC")
 suspend fun getHistoryByUserOnce(email: String): List<HistoryEntity>

 @Query("SELECT * FROM history WHERE syncedWithApi = 0 AND userEmail = :email")
 suspend fun getPendingSync(email: String): List<HistoryEntity>

 @Insert(onConflict = OnConflictStrategy.REPLACE)
 suspend fun insert(history: HistoryEntity): Long

 @Update
 suspend fun update(history: HistoryEntity)

 @Delete
 suspend fun delete(history: HistoryEntity)

 @Query("DELETE FROM history WHERE userEmail = :email")
 suspend fun deleteAllByUser(email: String)
}

