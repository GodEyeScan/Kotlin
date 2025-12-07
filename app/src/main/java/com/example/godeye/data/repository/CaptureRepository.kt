package com.example.godeye.data.repository

import com.example.godeye.data.CaptureData
import com.example.godeye.data.database.CaptureDao
import com.example.godeye.data.database.CaptureEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository Pattern Implementation
 * Abstrae el acceso a datos de capturas, proporcionando una interfaz limpia
 * para el ViewModel sin exponer detalles de la base de datos.
 */
@Singleton
/**
 * CaptureRepository
 *
 * Descripción: [Agregar descripción]
 *
 * @author GodEye Team
 * @version 1.0
 * @since 2025-12-07
 */
class CaptureRepository @Inject constructor(
 private val captureDao: CaptureDao
) {

 /**
 * Obtiene todas las capturas como Flow observable
 * @return Flow de lista de CaptureData ordenadas por timestamp descendente
 */
 fun getAllCaptures(): Flow<List<CaptureData>> {
 return captureDao.getAllCaptures().map { entities ->
 entities.map { it.toCaptureData() }
 }
 }

 /**
 * Obtiene una captura específica por ID
 * @param id ID de la captura
 * @return CaptureData o null si no existe
 */
 suspend fun getCaptureById(id: Long): CaptureData? {
 return captureDao.getCaptureById(id)?.toCaptureData()
 }

 /**
 * Obtiene capturas por placa detectada
 * @param plate Placa a buscar
 * @return Flow de lista de CaptureData con esa placa
 */
 fun getCapturesByPlate(plate: String): Flow<List<CaptureData>> {
 return captureDao.getCapturesByPlate(plate).map { entities ->
 entities.map { it.toCaptureData() }
 }
 }

 /**
 * Inserta una nueva captura en la base de datos
 * @param captureData Datos de la captura
 * @return ID de la captura insertada
 */
 suspend fun insertCapture(captureData: CaptureData): Long {
 return captureDao.insert(captureData.toEntity())
 }

 /**
 * Actualiza una captura existente
 * @param captureData Datos actualizados
 */
 suspend fun updateCapture(captureData: CaptureData) {
 captureDao.update(captureData.toEntity())
 }

 /**
 * Elimina una captura específica
 * @param captureData Captura a eliminar
 */
 suspend fun deleteCapture(captureData: CaptureData) {
 captureDao.delete(captureData.toEntity())
 }

 /**
 * Elimina todas las capturas
 */
 suspend fun deleteAllCaptures() {
 captureDao.deleteAll()
 }

 /**
 * Obtiene el número total de capturas
 * @return Cantidad de capturas en la base de datos
 */
 suspend fun getCaptureCount(): Int {
 return captureDao.getCaptureCount()
 }
}

/**
 * Extension Functions - Data Mapper Pattern
 * Convierte entre CaptureData (modelo de dominio) y CaptureEntity (modelo de persistencia)
 */

private fun CaptureData.toEntity(): CaptureEntity {
 return CaptureEntity(
 id = id,
 imageUri = imageUri,
 latitude = latitude,
 longitude = longitude,
 timestamp = timestamp,
 extractedText = extractedText,
 detectedPlate = detectedPlate ?: ""
 )
}

private fun CaptureEntity.toCaptureData(): CaptureData {
 return CaptureData(
 id = id,
 imageUri = imageUri,
 latitude = latitude,
 longitude = longitude,
 timestamp = timestamp,
 extractedText = extractedText,
 detectedPlate = detectedPlate.takeIf { it.isNotEmpty() }
 )
}

