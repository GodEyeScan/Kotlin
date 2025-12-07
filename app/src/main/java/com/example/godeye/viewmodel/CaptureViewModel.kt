/**
 * CaptureViewModel.kt
 *
 * ViewModel que gestiona el estado relacionado con las capturas realizadas
 * por el usuario (historial, selección, filtros, etc.).
 *
 * Notas:
 * - Documentación general, destinada a facilitar la lectura y mantenimiento.
 * - No se modificó la lógica, solo se añadió documentación de cabecera.
 */

package com.example.godeye.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.godeye.data.CaptureData
import com.example.godeye.data.database.CaptureEntity
import com.example.godeye.data.database.GodEyeDatabase
import com.example.godeye.data.repository.ReportRepository
import com.example.godeye.data.repository.HistoryRepository
import com.example.godeye.data.repository.ApiResult
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import java.text.SimpleDateFormat
import java.util.*

/**
 * CaptureViewModel
 *
 * Descripción: [Agregar descripción]
 *
 * @author GodEye Team
 * @version 1.0
 * @since 2025-12-07
 */
class CaptureViewModel(application: Application) : AndroidViewModel(application) {

 private val database = GodEyeDatabase.getDatabase(application)
 private val captureDao = database.captureDao()
 private val reportDao = database.reportDao()
 private val userProfileDao = database.userProfileDao()
 private val historyDao = database.historyDao()

 // Repositorios de la API
 private val reportRepository = ReportRepository()
 private val historyRepository = HistoryRepository()

 // Lista observable para la UI
 private val _captures = mutableStateListOf<CaptureData>()
 val captures: List<CaptureData> = _captures

 // Estado de carga
 private val _isLoading = mutableStateOf(false)
 val isLoading = _isLoading

 // Mensaje de error
 private val _errorMessage = mutableStateOf<String?>(null)
 val errorMessage = _errorMessage

 // Estado de alerta de placa reportada
 private val _plateAlert = mutableStateOf<PlateAlert?>(null)
 val plateAlert = _plateAlert

 // Email del usuario actual (para filtrar capturas)
 private var currentUserEmail: String? = null

 // Job para controlar el Flow de capturas
 private var capturesJob: Job? = null

 init {
 // No cargar nada automáticamente, esperar que se establezca el usuario
 }

 /**
 * Establece el email del usuario actual y carga sus capturas
 */
 fun setCurrentUser(email: String) {
 android.util.Log.d("CaptureViewModel", "setCurrentUser llamado para: $email")

 // Cancelar el Job anterior (detiene el Flow anterior)
 capturesJob?.cancel()

 // Limpiar datos del usuario anterior
 _captures.clear()
 _plateAlert.value = null
 _errorMessage.value = null
 _isLoading.value = false

 android.util.Log.d("CaptureViewModel", "Estado limpiado. Captures size: ${_captures.size}")

 // Establecer nuevo usuario y cargar sus datos
 currentUserEmail = email
 loadCapturesForUser(email)
 }

 /**
 * Limpia los datos del usuario actual (usar al hacer logout)
 */
 fun clearUserData() {
 android.util.Log.d("CaptureViewModel", "clearUserData llamado")

 // Cancelar el Job (detiene el Flow)
 capturesJob?.cancel()
 capturesJob = null

 currentUserEmail = null
 _captures.clear()
 _plateAlert.value = null
 _errorMessage.value = null
 _isLoading.value = false

 android.util.Log.d("CaptureViewModel", "Datos limpiados completamente. Captures size: ${_captures.size}")
 }

 private fun loadCapturesFromDatabase() {
 viewModelScope.launch {
 captureDao.getAllCaptures().collect { entities ->
 _captures.clear()
 _captures.addAll(entities.map { it.toCaptureData() })
 }
 }
 }

 /**
 * Carga solo las capturas del usuario especificado
 */
 private fun loadCapturesForUser(email: String) {
 android.util.Log.d("CaptureViewModel", "loadCapturesForUser iniciado para: $email")

 capturesJob = viewModelScope.launch {
 captureDao.getCapturesByUser(email).collect { entities ->
 android.util.Log.d("CaptureViewModel", "Flow emitió ${entities.size} capturas para: $email")
 _captures.clear()
 _captures.addAll(entities.map { it.toCaptureData() })
 android.util.Log.d("CaptureViewModel", "Captures actualizado. Size: ${_captures.size}")
 }
 }
 }

 /**
 * Carga todos los reportes desde la API
 * Usado cuando un administrador necesita ver todos los reportes
 */
 fun getAllReportsFromApi(
 token: String?,
 onComplete: (Boolean, List<CaptureData>?, String?) -> Unit
 ) {
 viewModelScope.launch {
 if (token == null) {
 onComplete(false, null, "Token no disponible")
 return@launch
 }

 _isLoading.value = true
 _errorMessage.value = null

 when (val result = reportRepository.getReportsAdmin(token)) {
 is ApiResult.Success -> {
 // Convertir los reportes de la API a CaptureData
 val apiCaptures = result.data.map { report ->
 CaptureData(
 id = report.id?.toLong() ?: System.currentTimeMillis(),
 userEmail = "", // No sabemos el email desde la API
 imageUri = "", // Los reportes de otros no tienen imagen local
 latitude = 0.0, // Sin ubicación específica
 longitude = 0.0,
 timestamp = parseTimestamp(report.timestamp) ?: System.currentTimeMillis(),
 extractedText = "${report.type} - ${report.color}",
 detectedPlate = report.placa
 )
 }

 _isLoading.value = false
 onComplete(true, apiCaptures, null)
 }
 is ApiResult.Error -> {
 _errorMessage.value = result.message
 _isLoading.value = false
 onComplete(false, null, result.message)
 }
 }
 }
 }

 /**
 * Obtiene todas las capturas de una placa específica del usuario actual
 */
 suspend fun getCapturesByPlate(plate: String): List<CaptureData> {
 val email = currentUserEmail ?: return emptyList()
 val flow = captureDao.getCapturesByPlateAndUser(plate, email)
 val entities = flow.first()
 return entities.map { it.toCaptureData() }
 }

 /**
 * Obtiene TODAS las capturas de una placa de TODOS los usuarios
 * Usado para el tracking en el mapa
 */
 suspend fun getAllCapturesByPlateAllUsers(plate: String): List<CaptureData> {
 android.util.Log.d("CaptureViewModel", "Buscando TODAS las capturas de placa: $plate (todos los usuarios)")
 val flow = captureDao.getCapturesByPlate(plate)
 val entities = flow.first()
 android.util.Log.d("CaptureViewModel", "Encontradas ${entities.size} ubicaciones totales para placa: $plate")
 return entities.map { it.toCaptureData() }
 }

 /**
 * Depuración: Obtiene el conteo de capturas por usuario
 */
 suspend fun debugCaptureCount(): Map<String, Int> {
 val allCaptures = captureDao.getAllCapturesOnce()
 return allCaptures.groupBy { it.userEmail }
 .mapValues { it.value.size }
 }

 /**
 * Obtiene todos los reportes de la base de datos local
 * Útil como respaldo cuando no hay conexión
 */
 fun getAllCapturesFromLocal(onComplete: (List<CaptureData>) -> Unit) {
 viewModelScope.launch {
 captureDao.getAllCapturesOnce().let { entities ->
 val captures = entities.map { it.toCaptureData() }
 onComplete(captures)
 }
 }
 }

 /**
 * Carga todos los reportes desde la API
 * Usado cuando un administrador necesita ver todos los reportes
 */
 fun loadAllReportsFromApi(token: String) {
 viewModelScope.launch {
 _isLoading.value = true
 _errorMessage.value = null

 when (val result = reportRepository.getReportsAdmin(token)) {
 is ApiResult.Success -> {
 // Convertir los reportes de la API a CaptureData
 val apiCaptures = result.data.map { report ->
 CaptureData(
 id = report.id?.toLong() ?: System.currentTimeMillis(),
 userEmail = "", // No sabemos el email desde la API
 imageUri = "", // Los reportes de otros no tienen imagen local
 latitude = 0.0, // Sin ubicación específica
 longitude = 0.0,
 timestamp = parseTimestamp(report.timestamp) ?: System.currentTimeMillis(),
 extractedText = "${report.type} - ${report.color}",
 detectedPlate = report.placa
 )
 }

 _captures.clear()
 _captures.addAll(apiCaptures)
 _isLoading.value = false
 }
 is ApiResult.Error -> {
 _errorMessage.value = result.message
 _isLoading.value = false
 }
 }
 }
 }

 /**
 * Carga los reportes del usuario desde la API
 */
 fun loadUserReportsFromApi(token: String) {
 viewModelScope.launch {
 _isLoading.value = true
 _errorMessage.value = null

 // Por ahora usamos la base de datos local + sincronizamos
 loadCapturesFromDatabase()
 _isLoading.value = false
 }
 }

 /**
 * Agrega una captura localmente y la envía a la API
 * SIEMPRE guarda localmente primero, luego intenta sincronizar con API
 * Si la API falla, el usuario aún tiene sus datos guardados localmente
 */
 fun addCapture(
 captureData: CaptureData,
 token: String?,
 userEmail: String,
 onComplete: ((Boolean, String?) -> Unit)? = null
 ) {
 viewModelScope.launch {
 try {
 // 1. SIEMPRE guardar en la base de datos local primero (prioridad)
 val entity = captureData.toEntity()
 val insertedId = captureDao.insert(entity)

 // 2. Obtener el perfil del usuario para el reporte local
 val profile = userProfileDao.getProfile(userEmail)

 // 3. Crear reporte local (siempre, independiente de la API)
 if (captureData.detectedPlate != null) {
 val reportEntity = com.example.godeye.data.database.ReportEntity(
 userEmail = userEmail,
 userName = profile?.name ?: "",
 userPhone = profile?.phone ?: "",
 userNit = profile?.nit ?: "",
 plateNumber = captureData.detectedPlate,
 reportReason = captureData.extractedText.ifEmpty { "Detección automática" },
 timestamp = captureData.timestamp
 )
 reportDao.insert(reportEntity)
 }

 // 3.5. Guardar historial local (foto + GPS + timestamp)
 if (captureData.imageUri.isNotEmpty()) {
 val historyEntity = com.example.godeye.data.database.HistoryEntity(
 userEmail = userEmail,
 photoUri = captureData.imageUri,
 latitude = captureData.latitude,
 longitude = captureData.longitude,
 timestamp = captureData.timestamp,
 syncedWithApi = false // Aún no sincronizado
 )
 historyDao.insert(historyEntity)
 }

 // 4. Intentar sincronizar con la API (no bloqueante)
 var apiMessage: String? = null
 var apiSynced = false
 token?.let { authToken ->
 try {
 // Primero verificar si la placa ya existe en el sistema
 val placa = captureData.detectedPlate
 if (placa != null) {
                android.util.Log.d("CaptureViewModel", "Verificando si placa $placa ya existe antes de crear reporte...")

 when (val checkResult = reportRepository.searchReportByPlate(authToken, placa)) {
 is ApiResult.Success -> {
                        val existingReports = checkResult.data

                        if (existingReports.isNotEmpty()) {
                            // La placa YA existe en el servidor - MARCAR COMO REPORTADA
                            android.util.Log.w("CaptureViewModel", "Placa $placa ya tiene ${existingReports.size} reporte(s) en el servidor")
                            android.util.Log.w("CaptureViewModel", "NO se creará reporte duplicado")

                            // Actualizar el registro recién insertado para marcarlo como reportado
                            val updatedEntity = entity.copy(id = insertedId, isReported = true)
                            captureDao.update(updatedEntity)

                            apiMessage = "Guardado localmente. Placa ya reportada previamente (${existingReports.size} veces)"
                            apiSynced = false
                        } else {
                            // La placa NO existe, proceder a crear reporte
                            android.util.Log.i("CaptureViewModel", "Placa $placa no existe en servidor, creando reporte...")

                            // Intentar crear reporte en la API
                            val reportCreated = createReportInApi(captureData, authToken)

                            // Intentar crear historial en la API
                            val historyCreated = createHistoryInApi(captureData, authToken)

                            if (!reportCreated || !historyCreated) {
                                apiMessage = "Guardado localmente. Sincronización con servidor fallida."
                            } else {
                                apiMessage = "Guardado y sincronizado exitosamente"
                                apiSynced = true

                                // Actualizar flag de sincronización en historial local
                                if (captureData.imageUri.isNotEmpty()) {
                                    val histories = historyDao.getHistoryByUserOnce(userEmail)
                                    histories.lastOrNull()?.let { lastHistory ->
                                        historyDao.update(lastHistory.copy(syncedWithApi = true))
                                    }
                                }
                            }
                        }
                    }
                    is ApiResult.Error -> {
                        android.util.Log.e("CaptureViewModel", "Error al verificar placa: ${checkResult.message}")
                        android.util.Log.w("CaptureViewModel", "Se omitirá POST por seguridad (evitar duplicados)")
                        apiMessage = "Guardado localmente. No se pudo verificar duplicados."
                    }
 }
                 } else {
                     // No hay placa detectada, guardar solo historial
                     val historyCreated = createHistoryInApi(captureData, authToken)
                     apiMessage = if (historyCreated) {
                         "Guardado localmente y historial sincronizado"
                     } else {
                         "Guardado localmente"
                     }
                 }
             } catch (e: Exception) {
                 android.util.Log.e("CaptureViewModel", "Excepción al sincronizar: ${e.message}", e)
                 apiMessage = "Guardado localmente. Sin conexión al servidor."
             }
         }

         // 5. Retornar éxito ya que se guardó localmente
         onComplete?.invoke(true, apiMessage ?: "Guardado localmente")

     } catch (e: Exception) {
         // Error crítico al guardar localmente
         onComplete?.invoke(false, "Error al guardar: ${e.localizedMessage}")
     }
 }
 }

 /**
 * Crea un reporte en la API
 */
 private suspend fun createReportInApi(captureData: CaptureData, token: String): Boolean {
 val plate = captureData.detectedPlate ?: return false
 val timestamp = formatTimestamp(captureData.timestamp)

        android.util.Log.d("CaptureViewModel", "POST /reports - Placa: $plate")

        return when (val result = reportRepository.createReport(
            token = token,
            placa = plate,
            timestamp = timestamp,
            type = "vehiculo", // Tipo genérico
            color = "desconocido" // Color genérico por ahora
        )) {
            is ApiResult.Success -> {
                android.util.Log.i("CaptureViewModel", "POST /reports exitoso - Placa: $plate, ID: ${result.data.id}")
                true // Reporte creado exitosamente
            }
            is ApiResult.Error -> {
                android.util.Log.e("CaptureViewModel", "POST /reports falló - Placa: $plate, Error: ${result.message}")
 _errorMessage.value = "Error al crear reporte: ${result.message}"
 false
 }
 }
 }

 /**
 * Crea un registro de historial en la API con la foto y GPS
 */
 private suspend fun createHistoryInApi(captureData: CaptureData, token: String): Boolean {
 // Convertir la URI local a una URL accesible
 // Por ahora usamos la URI directamente, en producción deberías subir la imagen a un servidor
 val photoUrl = captureData.imageUri

        android.util.Log.d("CaptureViewModel", "POST /history - GPS: (${captureData.latitude}, ${captureData.longitude})")

        return when (val result = historyRepository.createHistoryNow(
            token = token,
            photo = photoUrl,
            latitude = captureData.latitude,
            longitude = captureData.longitude
        )) {
            is ApiResult.Success -> {
                android.util.Log.i("CaptureViewModel", "POST /history exitoso - ID: ${result.data.id}")
                true // Historial creado exitosamente
            }
            is ApiResult.Error -> {
                android.util.Log.e("CaptureViewModel", "POST /history falló - Error: ${result.message}")
 _errorMessage.value = "Error al crear historial: ${result.message}"
 false
 }
 }
 }

 fun deleteCapture(captureData: CaptureData) {
 viewModelScope.launch {
 val entity = captureData.toEntity()
 captureDao.delete(entity)
 }
 }

 fun deleteAllCaptures() {
 viewModelScope.launch {
 captureDao.deleteAll()
 }
 }

 /**
 * Verifica si una placa existe en el sistema consultando la API
 * Emite una alerta si la placa está reportada
 *
 * @param token Token de autenticación
 * @param placa Placa a buscar (ej: "ABC123")
 * @param onResult Callback con el resultado (encontrada: Boolean, cantidad: Int)
 */
 fun checkPlateInSystem(
 token: String?,
 placa: String,
 onResult: ((Boolean, Int) -> Unit)? = null
 ) {
 if (token == null) {
 android.util.Log.w("CaptureViewModel", "No se puede buscar placa: Token no disponible")
 onResult?.invoke(false, 0)
 return
 }

 viewModelScope.launch {
 try {
 android.util.Log.d("CaptureViewModel", "GET /reports/check/$placa")
 android.util.Log.d("CaptureViewModel", "Buscando placa en el sistema: $placa")

 when (val result = reportRepository.searchReportByPlate(token, placa)) {
 is ApiResult.Success -> {
 val reportes = result.data
 val encontrada = reportes.isNotEmpty()

 if (encontrada) {
 android.util.Log.i("CaptureViewModel", "¡PLACA ENCONTRADA! $placa - ${reportes.size} reporte(s) en el sistema")
 reportes.forEachIndexed { index, report ->
 android.util.Log.d("CaptureViewModel", "Reporte #${index + 1}: ID=${report.id}, Tipo=${report.type}, Color=${report.color}")
 }

 // EMITIR ALERTA DE PLACA REPORTADA
 _plateAlert.value = PlateAlert(
 placa = placa,
 cantidadReportes = reportes.size,
 timestamp = System.currentTimeMillis()
 )
 } else {
 android.util.Log.i("CaptureViewModel", "Placa NO encontrada: $placa - No hay reportes en el sistema")
 }

 onResult?.invoke(encontrada, reportes.size)
 }
 is ApiResult.Error -> {
 android.util.Log.e("CaptureViewModel", "Error al buscar placa $placa: ${result.message}")
 onResult?.invoke(false, 0)
 }
 }
 } catch (e: Exception) {
 android.util.Log.e("CaptureViewModel", "Excepción al buscar placa $placa: ${e.message}", e)
 onResult?.invoke(false, 0)
 }
 }
 }

 /**
 * Limpia la alerta actual de placa reportada
 */
 fun clearPlateAlert() {
 _plateAlert.value = null
 }

 private fun parseTimestamp(timestamp: String?): Long? {
 return try {
 val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
 format.parse(timestamp ?: "")?.time
 } catch (e: Exception) {
 null
 }
 }

 private fun formatTimestamp(timestamp: Long): String {
 val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
 return format.format(Date(timestamp))
 }
}

// Extensiones para convertir entre CaptureData y CaptureEntity
private fun CaptureData.toEntity(): CaptureEntity {
 return CaptureEntity(
 id = id,
 userEmail = userEmail,
 imageUri = imageUri,
 latitude = latitude,
 longitude = longitude,
 timestamp = timestamp,
 extractedText = extractedText,
 detectedPlate = detectedPlate ?: "",
 isReported = isReported
 )
}

private fun CaptureEntity.toCaptureData(): CaptureData {
 return CaptureData(
 id = id,
 userEmail = userEmail,
 imageUri = imageUri,
 latitude = latitude,
 longitude = longitude,
 timestamp = timestamp,
 extractedText = extractedText,
 detectedPlate = detectedPlate.takeIf { it.isNotEmpty() },
 isReported = isReported
 )
}

/**
 * Clase de datos para representar una alerta de placa reportada
 */
data class PlateAlert(
 val placa: String,
 val cantidadReportes: Int,
 val timestamp: Long
)
