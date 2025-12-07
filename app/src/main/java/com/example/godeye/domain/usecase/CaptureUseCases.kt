/**
 * CaptureUseCases.kt
 *
 * Casos de uso relacionados con las capturas: obtener, filtrar y procesar resultados.
 */

package com.example.godeye.domain.usecase

import com.example.godeye.data.CaptureData
import com.example.godeye.data.repository.CaptureRepository
import com.example.godeye.utils.PlateDetector
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import javax.inject.Inject

/**
 * Use Case: Obtener todas las capturas
 * Encapsula la lógica de negocio para obtener capturas desde el repositorio
 */
class GetAllCapturesUseCase @Inject constructor(
 private val repository: CaptureRepository
) {
 operator fun invoke(): Flow<List<CaptureData>> {
 Timber.d("GetAllCapturesUseCase: Obteniendo todas las capturas")
 return repository.getAllCaptures()
 }
}

/**
 * Use Case: Guardar una nueva captura
 * Encapsula la lógica de negocio para procesar y guardar una captura
 * Incluye validaciones y procesamiento de OCR
 */
class SaveCaptureUseCase @Inject constructor(
 private val repository: CaptureRepository,
 private val plateDetector: PlateDetector
) {
 suspend operator fun invoke(captureData: CaptureData): Result<Long> {
 return try {
 Timber.d("SaveCaptureUseCase: Guardando captura con placa: ${captureData.detectedPlate}")

 // Validar que la captura tenga datos mínimos
 if (captureData.imageUri.isEmpty()) {
 Timber.w("SaveCaptureUseCase: URI de imagen vacía")
 return Result.failure(IllegalArgumentException("URI de imagen no puede estar vacía"))
 }

 // Si hay texto extraído pero no hay placa, intentar detectarla
 val processedCapture = if (captureData.extractedText.isNotEmpty() && captureData.detectedPlate == null) {
 val detectedPlate = plateDetector.detectPlate(captureData.extractedText)
 Timber.d("SaveCaptureUseCase: Placa detectada en procesamiento: $detectedPlate")
 captureData.copy(detectedPlate = detectedPlate)
 } else {
 captureData
 }

 val captureId = repository.insertCapture(processedCapture)
 Timber.i("SaveCaptureUseCase: Captura guardada exitosamente con ID: $captureId")
 Result.success(captureId)

 } catch (e: Exception) {
 Timber.e(e, "SaveCaptureUseCase: Error al guardar captura")
 Result.failure(e)
 }
 }
}

/**
 * Use Case: Eliminar una captura
 * Encapsula la lógica de negocio para eliminar capturas con validaciones
 */
class DeleteCaptureUseCase @Inject constructor(
 private val repository: CaptureRepository
) {
 suspend operator fun invoke(captureData: CaptureData): Result<Unit> {
 return try {
 Timber.d("DeleteCaptureUseCase: Eliminando captura con placa: ${captureData.detectedPlate}")
 repository.deleteCapture(captureData)
 Timber.i("DeleteCaptureUseCase: Captura eliminada exitosamente")
 Result.success(Unit)
 } catch (e: Exception) {
 Timber.e(e, "DeleteCaptureUseCase: Error al eliminar captura")
 Result.failure(e)
 }
 }
}

/**
 * Use Case: Buscar capturas por placa
 * Encapsula la lógica de negocio para buscar capturas de una placa específica
 */
class SearchCapturesByPlateUseCase @Inject constructor(
 private val repository: CaptureRepository
) {
 operator fun invoke(plate: String?): Flow<List<CaptureData>> {
 Timber.d("SearchCapturesByPlateUseCase: Buscando capturas con placa: $plate")

 // Normalizar la placa (mayúsculas, sin espacios)
 val normalizedPlate = plate?.uppercase()?.trim() ?: ""

 if (normalizedPlate.isEmpty()) {
 Timber.w("SearchCapturesByPlateUseCase: Placa vacía proporcionada")
 }

 return repository.getCapturesByPlate(normalizedPlate)
 }
}

/**
 * Use Case: Obtener estadísticas de capturas
 * Encapsula la lógica de negocio para calcular estadísticas
 */
class GetCaptureStatisticsUseCase @Inject constructor(
 private val repository: CaptureRepository
) {
 suspend operator fun invoke(): Result<CaptureStatistics> {
 return try {
 Timber.d("GetCaptureStatisticsUseCase: Calculando estadísticas")
 val count = repository.getCaptureCount()

 val statistics = CaptureStatistics(
 totalCaptures = count,
 // Aquí se pueden agregar más estadísticas en el futuro
 )

 Timber.d("GetCaptureStatisticsUseCase: Estadísticas calculadas - Total: $count")
 Result.success(statistics)

 } catch (e: Exception) {
 Timber.e(e, "GetCaptureStatisticsUseCase: Error al calcular estadísticas")
 Result.failure(e)
 }
 }
}

/**
 * Data class para estadísticas de capturas
 */
data class CaptureStatistics(
 val totalCaptures: Int,
 // Agregar más campos según sea necesario
 // val capturesThisWeek: Int,
 // val capturesThisMonth: Int,
 // val uniquePlates: Int
)
