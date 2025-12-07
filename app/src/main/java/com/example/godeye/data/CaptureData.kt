package com.example.godeye.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
/**
 * CaptureData
 *
 * Descripción: [Agregar descripción]
 *
 * @author GodEye Team
 * @version 1.0
 * @since 2025-12-07
 */
data class CaptureData(
 val id: Long = 0,
 val userEmail: String = "", // Email del usuario propietario
 val imageUri: String,
 val latitude: Double,
 val longitude: Double,
 val timestamp: Long,
 val extractedText: String = "",
 val detectedPlate: String? = null,
 val isReported: Boolean = false // Indica si la placa está reportada
) : Parcelable

