package com.example.godeye.data.api

import com.google.gson.annotations.SerializedName

/**
 * Modelo de solicitud para crear un reporte de vehículo
 *
 * Endpoint: POST /reports
 * URL: https://gateway.helmer-pardo.com/reports
 * Autenticación: Bearer token requerido
 */
data class CreateReportRequest(
 @SerializedName("placa")
 val placa: String,

 @SerializedName("timestamp")
 val timestamp: String,

 @SerializedName("type")
 val type: String,

 @SerializedName("color")
 val color: String
)

/**
 * Modelo de respuesta al crear un reporte
 */
data class ReportResponse(
 @SerializedName("id")
 val id: String? = null,

 @SerializedName("placa")
 val placa: String,

 @SerializedName("timestamp")
 val timestamp: String,

 @SerializedName("type")
 val type: String,

 @SerializedName("color")
 val color: String,

 @SerializedName("userId")
 val userId: String? = null,

 @SerializedName("createdAt")
 val createdAt: String? = null,

 @SerializedName("updatedAt")
 val updatedAt: String? = null,

 @SerializedName("message")
 val message: String? = null
)

/**
 * Modelo de respuesta para verificar si una placa existe
 * Endpoint: GET /reports/check/{placa}
 *
 * Estructura real del servidor:
 * {
 * "status": 200,
 * "exists": true,
 * "data": { reporte }
 * }
 */
data class CheckPlateResponse(
 @SerializedName("status")
 val status: Int = 200,

 @SerializedName("exists")
 val exists: Boolean = false,

 @SerializedName("data")
 val data: ReportResponse? = null,

 @SerializedName("message")
 val message: String? = null
)

/**
 * Modelo genérico de error de API
 */
data class ApiError(
 @SerializedName("error")
 val error: String? = null,

 @SerializedName("message")
 val message: String? = null,

 @SerializedName("statusCode")
 val statusCode: Int? = null
) {
 fun getErrorMessage(): String {
 return message ?: error ?: "Error desconocido"
 }
}

