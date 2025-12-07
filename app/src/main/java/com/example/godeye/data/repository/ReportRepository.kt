package com.example.godeye.data.repository

import com.example.godeye.data.api.*
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

/**
 * Repositorio para manejar los reportes de vehículos con la API en la nube
 *
 * Endpoints:
 * - POST /reports - Crear nuevo reporte (requiere autenticación)
 * - GET /reports - Obtener todos los reportes del usuario
 * - GET /reports/{id} - Obtener un reporte específico
 */
class ReportRepository {

 private val apiService = RetrofitClient.apiService
 private val gson = Gson()

 /**
 * Crea un nuevo reporte de vehículo
 *
 * @param token Token de autenticación Bearer
 * @param placa Placa/matrícula del vehículo (ej: "XYZ789")
 * @param timestamp Fecha y hora en formato ISO 8601 (ej: "2025-12-06T18:45:30-05:00")
 * @param type Tipo de vehículo (ej: "moto", "carro")
 * @param color Color del vehículo (ej: "rojo", "azul")
 * @return ApiResult con el reporte creado o error
 */
 suspend fun createReport(
 token: String,
 placa: String,
 timestamp: String,
 type: String,
 color: String
 ): ApiResult<ReportResponse> {
 return withContext(Dispatchers.IO) {
 try {
 val request = CreateReportRequest(
 placa = placa,
 timestamp = timestamp,
 type = type,
 color = color
 )
            android.util.Log.d("ReportRepository", "Preparando POST /reports")
            android.util.Log.d("ReportRepository", "Placa: $placa")
            android.util.Log.d("ReportRepository", "Timestamp: $timestamp")
            android.util.Log.d("ReportRepository", "Type: $type")
            android.util.Log.d("ReportRepository", "Color: $color")
            android.util.Log.d("ReportRepository", "Token: ${token.take(20)}...")

            val authorization = "Bearer $token"
            val response = apiService.createReport(authorization, request)

            android.util.Log.d("ReportRepository", "Respuesta recibida: ${response.code()}")
            if (!response.isSuccessful) {
                android.util.Log.e("ReportRepository", "Error body: ${response.errorBody()?.string()}")
            }

            handleReportResponse(response)
        } catch (e: Exception) {
            android.util.Log.e("ReportRepository", "Excepción en POST /reports: ${e.message}", e)
            android.util.Log.e("ReportRepository", "Tipo: ${e.javaClass.simpleName}")
            android.util.Log.e("ReportRepository", "Placa que se intentaba reportar: $placa")
 ApiResult.Error("Error de conexión: ${e.localizedMessage}")
 }
 }
 }

 /**
 * Crea un reporte con timestamp actual automático
 */
 suspend fun createReportNow(
 token: String,
 placa: String,
 type: String,
 color: String
 ): ApiResult<ReportResponse> {
 val timestamp = getCurrentTimestampISO8601()
 return createReport(token, placa, timestamp, type, color)
 }

 /**
 * Obtiene todos los reportes del usuario actual
 *
 * @param token Token de autenticación Bearer
 * @return ApiResult con lista de reportes o error
 */
 suspend fun getReports(token: String): ApiResult<List<ReportResponse>> {
 return withContext(Dispatchers.IO) {
 try {
 val authorization = "Bearer $token"
 val response = apiService.getReports(authorization)
 handleReportsListResponse(response)
 } catch (e: Exception) {
 ApiResult.Error("Error de conexión: ${e.localizedMessage}")
 }
 }
 }

 /**
 * Obtiene un reporte específico por ID
 *
 * @param token Token de autenticación Bearer
 * @param reportId ID del reporte
 * @return ApiResult con el reporte o error
 */
 suspend fun getReportById(token: String, reportId: String): ApiResult<ReportResponse> {
 return withContext(Dispatchers.IO) {
 try {
 val authorization = "Bearer $token"
 val response = apiService.getReportById(authorization, reportId)
 handleReportResponse(response)
 } catch (e: Exception) {
 ApiResult.Error("Error de conexión: ${e.localizedMessage}")
 }
 }
 }

 /**
 * [ADMIN ONLY] Obtiene todos los reportes del sistema
 *
 * Este método solo funciona con un token de administrador.
 * Retorna todos los reportes de todos los usuarios.
 *
 * @param token Token de autenticación Bearer de un administrador
 * @return ApiResult con lista completa de reportes o error
 */
 suspend fun getReportsAdmin(token: String): ApiResult<List<ReportResponse>> {
 return withContext(Dispatchers.IO) {
 try {
 val authorization = "Bearer $token"
 val response = apiService.getReportsAdmin(authorization)
 handleReportsListResponse(response)
 } catch (e: Exception) {
 ApiResult.Error("Error de conexión: ${e.localizedMessage}")
 }
 }
 }

 /**
 * Busca reportes por placa específica
 *
 * @param token Token de autenticación Bearer
 * @param placa Placa del vehículo a buscar (ej: "ABC123")
 * @return ApiResult con lista de reportes de esa placa o error
 *
 * Uso:
 * ```
 * val result = searchReportByPlate(token, "ABC123")
 * when (result) {
 * is ApiResult.Success -> {
 * if (result.data.isNotEmpty()) {
 * Log.d("TAG", "¡Placa ABC123 ENCONTRADA en el sistema!")
 * } else {
 * Log.d("TAG", "Placa ABC123 NO encontrada")
 * }
 * }
 * is ApiResult.Error -> Log.e("TAG", "Error: ${result.message}")
 * }
 * ```
 */
 suspend fun searchReportByPlate(token: String, placa: String): ApiResult<List<ReportResponse>> {
 return withContext(Dispatchers.IO) {
        try {
            android.util.Log.d("ReportRepository", "GET /reports/check/$placa")
            val authorization = "Bearer $token"
            val response = apiService.searchReportByPlate(authorization, placa)

            android.util.Log.d("ReportRepository", "Respuesta HTTP: ${response.code()}")

            if (response.isSuccessful) {
                val checkResponse = response.body()
                if (checkResponse != null) {
                    android.util.Log.d("ReportRepository", "status: ${checkResponse.status}")
                    android.util.Log.d("ReportRepository", "exists: ${checkResponse.exists}")
                    android.util.Log.d("ReportRepository", "data.placa: ${checkResponse.data?.placa}")

                    // El servidor devuelve UN solo reporte en "data", no una lista
                    val reportsList = if (checkResponse.exists && checkResponse.data != null) {
                        android.util.Log.i("ReportRepository", "Placa encontrada: ${checkResponse.data.placa}")
                        listOf(checkResponse.data) // Convertir el reporte único a lista
                    } else {
                        android.util.Log.i("ReportRepository", "Placa no encontrada")
                        emptyList()
                    }

                    ApiResult.Success(reportsList)
                } else {
                    android.util.Log.w("ReportRepository", "Body vacío")
                    ApiResult.Success(emptyList())
                }
            } else {
                val errorBody = response.errorBody()?.string()
                android.util.Log.e("ReportRepository", "Error HTTP ${response.code()}: $errorBody")
                ApiResult.Error("Error: ${response.code()}")
            }
        } catch (e: Exception) {
            android.util.Log.e("ReportRepository", "Excepción: ${e.message}", e)
            android.util.Log.e("ReportRepository", "Tipo: ${e.javaClass.simpleName}")
            ApiResult.Error("Error de conexión: ${e.localizedMessage}")
        }
    }
}

 /**
 * Maneja la respuesta de un reporte individual
 */
 private fun handleReportResponse(response: Response<ReportResponse>): ApiResult<ReportResponse> {
 return if (response.isSuccessful) {
 val reportResponse = response.body()
 if (reportResponse != null) {
 ApiResult.Success(reportResponse)
 } else {
 ApiResult.Error("Respuesta vacía del servidor")
 }
 } else {
 val errorBody = response.errorBody()?.string()
 val apiError = try {
 gson.fromJson(errorBody, ApiError::class.java)
 } catch (e: Exception) {
 null
 }
 ApiResult.Error(apiError?.getErrorMessage() ?: "Error: ${response.code()}")
 }
 }

 /**
 * Maneja la respuesta de lista de reportes
 */
 private fun handleReportsListResponse(response: Response<List<ReportResponse>>): ApiResult<List<ReportResponse>> {
 return if (response.isSuccessful) {
 val reports = response.body()
 if (reports != null) {
 ApiResult.Success(reports)
 } else {
 ApiResult.Success(emptyList())
 }
 } else {
 val errorBody = response.errorBody()?.string()
 val apiError = try {
 gson.fromJson(errorBody, ApiError::class.java)
 } catch (e: Exception) {
 null
 }
 ApiResult.Error(apiError?.getErrorMessage() ?: "Error: ${response.code()}")
 }
 }

 /**
 * Genera un timestamp en formato ISO 8601 con zona horaria
 * Formato: "2025-12-06T18:45:30-05:00"
 */
 private fun getCurrentTimestampISO8601(): String {
 val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US)
 dateFormat.timeZone = TimeZone.getDefault()
 return dateFormat.format(Date())
 }
}

