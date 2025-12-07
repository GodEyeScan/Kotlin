package com.example.godeye.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "captures")
/**
 * CaptureEntity
 *
 * Descripción: [Agregar descripción]
 *
 * @author GodEye Team
 * @version 1.0
 * @since 2025-12-07
 */
data class CaptureEntity(
 @PrimaryKey(autoGenerate = true)
 val id: Long = 0,
 val userEmail: String = "", // Email del usuario que creó la captura
 val imageUri: String,
 val latitude: Double,
 val longitude: Double,
 val timestamp: Long,
 val extractedText: String = "",
 val detectedPlate: String = "",
 val isReported: Boolean = false // Indica si la placa está reportada en el sistema
)

@Entity(tableName = "reports")
data class ReportEntity(
 @PrimaryKey(autoGenerate = true)
 val id: Long = 0,
 val userEmail: String,
 val userName: String,
 val userPhone: String,
 val userNit: String,
 val plateNumber: String,
 val reportReason: String,
 val timestamp: Long
)

/**
 * Entidad para guardar el perfil del usuario localmente
 * Campos: nombre, teléfono, NIT (no se guardan en la nube)
 */
@Entity(tableName = "user_profile")
data class UserProfileEntity(
 @PrimaryKey
 val email: String,
 val name: String,
 val phone: String,
 val nit: String
)

/**
 * Entidad para guardar el historial de fotos localmente
 * Incluye foto, ubicación GPS y timestamp
 */
@Entity(tableName = "history")
data class HistoryEntity(
 @PrimaryKey(autoGenerate = true)
 val id: Long = 0,
 val userEmail: String, // Email del usuario propietario
 val photoUri: String, // URI de la foto local
 val latitude: Double,
 val longitude: Double,
 val timestamp: Long,
 val syncedWithApi: Boolean = false // Indica si se sincronizó con la API
)

