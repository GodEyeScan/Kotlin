package com.example.godeye.data.repository

import com.example.godeye.data.api.*
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Repositorio para manejar el historial de eventos con fotos y ubicaciones
 *
 * El módulo de historial almacena evidencias geolocalizadas con foto y fecha,
 * asociadas al usuario que reporta y posiblemente a una placa de vehículo.
 *
 * Endpoints:
 * - POST /history - Crear nuevo registro de historial (requiere autenticación)
 * - GET /history - Obtener todos los registros de historial del usuario
 */
class HistoryRepository {

 private val apiService = RetrofitClient.apiService
 private val gson = Gson()

 /**
 * Crea un nuevo registro en el historial con foto y ubicación
 *
 * @param token Token de autenticación Bearer
 * @param photo URL de la foto del evento (ej: "https://example.com/photo.jpg")
 * @param timestamp Fecha y hora en formato ISO 8601 (ej: "2025-12-07T12:34:56Z")
 * @param latitude Coordenada de latitud (ej: 4.6097)
 * @param longitude Coordenada de longitud (ej: -74.0817)
 * @return ApiResult con el historial creado o error
 */
 suspend fun createHistory(
 token: String,
 photo: String,
 timestamp: String,
 latitude: Double,
 longitude: Double
 ): ApiResult<HistoryResponse> {
 return withContext(Dispatchers.IO) {
 try {
 val request = CreateHistoryRequest(
 photo = photo,
 timestamp = timestamp,
 latitude = latitude,
 longitude = longitude
 )
 val authorization = "Bearer $token"
 val response = apiService.createHistory(authorization, request)
 handleHistoryResponse(response)
 } catch (e: Exception) {
 ApiResult.Error("Error de conexión: ${e.localizedMessage}")
 }
 }
 }

 /**
 * Crea un registro de historial con timestamp actual automático
 *
 * @param token Token de autenticación Bearer
 * @param photo URL de la foto
 * @param latitude Coordenada de latitud
 * @param longitude Coordenada de longitud
 * @return ApiResult con el historial creado o error
 */
 suspend fun createHistoryNow(
 token: String,
 photo: String,
 latitude: Double,
 longitude: Double
 ): ApiResult<HistoryResponse> {
 val timestamp = getCurrentTimestampISO8601()
 return createHistory(token, photo, timestamp, latitude, longitude)
 }

 /**
 * Obtiene todos los registros de historial del usuario actual
 *
 * @param token Token de autenticación Bearer
 * @return ApiResult con lista de registros de historial o error
 */
 suspend fun getHistory(token: String): ApiResult<List<HistoryResponse>> {
 return withContext(Dispatchers.IO) {
 try {
 val authorization = "Bearer $token"
 val response = apiService.getHistory(authorization)
 handleHistoryListResponse(response)
 } catch (e: Exception) {
 ApiResult.Error("Error de conexión: ${e.localizedMessage}")
 }
 }
 }

 /**
 * Sube una foto y crea un registro de historial asociado a un reporte
 *
 * POST /files/upload-history
 * Este endpoint recibe form-data con el archivo de imagen y los datos del historial.
 *
 * @param token Token de autenticación Bearer
 * @param photoFile Archivo de imagen a subir
 * @param reportId ID del reporte al que se asocia esta evidencia
 * @param timestamp Fecha y hora del evento en formato ISO 8601 (ej: "2025-12-07T15:30:00Z")
 * @param latitude Latitud donde se tomó la foto
 * @param longitude Longitud donde se tomó la foto
 * @return ApiResult con el historial creado incluyendo la URL de la foto subida
 *
 * Ejemplo de uso:
 * ```
 * val result = historyRepository.uploadHistoryWithPhoto(
 * token = userToken,
 * photoFile = File("/path/to/photo.jpg"),
 * reportId = "550e8400-e29b-41d4-a716-446655440000",
 * timestamp = "2025-12-07T15:30:00Z",
 * latitude = 4.6097,
 * longitude = -74.0817
 * )
 * when (result) {
 * is ApiResult.Success -> {
 * val history = result.data
 * // history.photo contiene la URL: "http://gateway.helmer-pardo.com/uploads/..."
 * Log.d("TAG", "Foto subida: ${history.photo}")
 * }
 * is ApiResult.Error -> Log.e("TAG", "Error: ${result.message}")
 * }
 * ```
 */
 suspend fun uploadHistoryWithPhoto(
 token: String,
 photoFile: File,
 reportId: String,
 timestamp: String,
 latitude: Double,
 longitude: Double
 ): ApiResult<HistoryResponse> {
 return withContext(Dispatchers.IO) {
 try {
 val authorization = "Bearer $token"

 // Crear el MultipartBody.Part para el archivo
 val requestFile = photoFile.asRequestBody("image/*".toMediaTypeOrNull())
 val filePart = MultipartBody.Part.createFormData("file", photoFile.name, requestFile)

 // Crear los RequestBody para los demás parámetros
 val reportIdBody = reportId.toRequestBody("text/plain".toMediaTypeOrNull())
 val timestampBody = timestamp.toRequestBody("text/plain".toMediaTypeOrNull())
 val latitudeBody = latitude.toString().toRequestBody("text/plain".toMediaTypeOrNull())
 val longitudeBody = longitude.toString().toRequestBody("text/plain".toMediaTypeOrNull())

 val response = apiService.uploadHistory(
 authorization = authorization,
 file = filePart,
 reportId = reportIdBody,
 timestamp = timestampBody,
 latitude = latitudeBody,
 longitude = longitudeBody
 )

 handleUploadHistoryResponse(response)
 } catch (e: Exception) {
 ApiResult.Error("Error al subir foto: ${e.localizedMessage}")
 }
 }
 }

 /**
 * Obtiene todo el historial asociado a un reporte específico
 *
 * GET /history/report/{reportId}
 * Recupera todas las fotos y posiciones relacionadas con un reporte.
 *
 * @param token Token de autenticación Bearer
 * @param reportId ID del reporte del cual se quiere obtener el historial
 * @return ApiResult con lista de registros de historial o error
 *
 * Ejemplo de uso:
 * ```
 * val result = historyRepository.getHistoryByReportId(
 * token = userToken,
 * reportId = "550e8400-e29b-41d4-a716-446655440000"
 * )
 * when (result) {
 * is ApiResult.Success -> {
 * val historyList = result.data
 * historyList.forEach { history ->
 * Log.d("TAG", "Foto: ${history.photo}, Lat: ${history.latitude}, Lng: ${history.longitude}")
 * }
 * }
 * is ApiResult.Error -> Log.e("TAG", "Error: ${result.message}")
 * }
 * ```
 */
 suspend fun getHistoryByReportId(
 token: String,
 reportId: String
 ): ApiResult<List<HistoryResponse>> {
 return withContext(Dispatchers.IO) {
 try {
 val authorization = "Bearer $token"
 val response = apiService.getHistoryByReportId(authorization, reportId)
 handleHistoryByReportResponse(response)
 } catch (e: Exception) {
 ApiResult.Error("Error al obtener historial del reporte: ${e.localizedMessage}")
 }
 }
 }

 /**
 * Maneja la respuesta de un registro de historial individual
 */
 private fun handleHistoryResponse(response: Response<HistoryResponse>): ApiResult<HistoryResponse> {
 return if (response.isSuccessful) {
 val historyResponse = response.body()
 if (historyResponse != null) {
 ApiResult.Success(historyResponse)
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
 * Maneja la respuesta de lista de registros de historial
 */
 private fun handleHistoryListResponse(response: Response<List<HistoryResponse>>): ApiResult<List<HistoryResponse>> {
 return if (response.isSuccessful) {
 val historyList = response.body()
 if (historyList != null) {
 ApiResult.Success(historyList)
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
 * Maneja la respuesta del endpoint de upload de historial con foto
 * Estructura: { "status": 200, "data": { historial } }
 */
 private fun handleUploadHistoryResponse(response: Response<UploadHistoryResponse>): ApiResult<HistoryResponse> {
 return if (response.isSuccessful) {
 val uploadResponse = response.body()
 if (uploadResponse != null && uploadResponse.status == 200) {
 ApiResult.Success(uploadResponse.data)
 } else {
 ApiResult.Error("Respuesta inválida del servidor")
 }
 } else {
 val errorBody = response.errorBody()?.string()
 val apiError = try {
 gson.fromJson(errorBody, ApiError::class.java)
 } catch (e: Exception) {
 null
 }
 ApiResult.Error(apiError?.getErrorMessage() ?: "Error al subir foto: ${response.code()}")
 }
 }

 /**
 * Maneja la respuesta del endpoint de historial por reportId
 * Estructura: { "status": 200, "data": [ {...}, {...} ] }
 */
 private fun handleHistoryByReportResponse(response: Response<ApiResponseWrapper<List<HistoryResponse>>>): ApiResult<List<HistoryResponse>> {
 return if (response.isSuccessful) {
 val wrapperResponse = response.body()
 if (wrapperResponse != null && wrapperResponse.status == 200) {
 ApiResult.Success(wrapperResponse.data)
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
 * Genera un timestamp en formato ISO 8601 con zona horaria UTC
 * Formato: "2025-12-07T12:34:56Z"
 */
 private fun getCurrentTimestampISO8601(): String {
 val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
 dateFormat.timeZone = TimeZone.getTimeZone("UTC")
 return dateFormat.format(Date())
 }
}

