package com.example.godeye.data.api

import com.google.gson.annotations.SerializedName

/**
 * Modelo de datos para crear un registro de historial
 *
 * Este modelo representa un evento geolocalizados con foto y fecha,
 * asociado a un usuario y posiblemente a una placa de vehículo.
 *
 * @property photo URL de la foto/imagen del evento (ej: foto del vehículo)
 * @property timestamp Marca de tiempo ISO 8601 con zona horaria (ej: "2025-12-07T12:34:56Z")
 * @property latitude Coordenada de latitud geográfica (ej: 4.6097)
 * @property longitude Coordenada de longitud geográfica (ej: -74.0817)
 */
data class CreateHistoryRequest(
 @SerializedName("photo")
 val photo: String,

 @SerializedName("timestamp")
 val timestamp: String,

 @SerializedName("latitude")
 val latitude: Double,

 @SerializedName("longitude")
 val longitude: Double
)

/**
 * Modelo de respuesta para un registro de historial
 *
 * Representa un registro de historial almacenado en la base de datos,
 * con información completa incluyendo ID, asociaciones y fechas de auditoría.
 *
 * @property id Identificador único del registro de historial
 * @property photo URL de la foto del evento
 * @property timestamp Marca de tiempo del evento
 * @property latitude Coordenada de latitud
 * @property longitude Coordenada de longitud
 * @property userId ID del usuario que creó el registro (opcional)
 * @property reportId ID del reporte asociado (opcional)
 * @property placa Placa del vehículo asociado (opcional)
 * @property createdAt Fecha de creación del registro
 * @property updatedAt Fecha de última actualización
 */
data class HistoryResponse(
 @SerializedName("id")
 val id: String? = null,

 @SerializedName("photo")
 val photo: String,

 @SerializedName("timestamp")
 val timestamp: String,

 @SerializedName("latitude")
 val latitude: Double,

 @SerializedName("longitude")
 val longitude: Double,

 @SerializedName("userId")
 val userId: String? = null,

 @SerializedName("reportId")
 val reportId: String? = null,

 @SerializedName("placa")
 val placa: String? = null,

 @SerializedName("createdAt")
 val createdAt: String? = null,

 @SerializedName("updatedAt")
 val updatedAt: String? = null
)

/**
 * Respuesta wrapper genérica de la API
 * Estructura: { "status": 200, "data": {...} }
 */
data class ApiResponseWrapper<T>(
 @SerializedName("status")
 val status: Int,

 @SerializedName("data")
 val data: T
)

/**
 * Modelo de respuesta para el endpoint de upload de historial con foto
 * POST /files/upload-history
 */
typealias UploadHistoryResponse = ApiResponseWrapper<HistoryResponse>

