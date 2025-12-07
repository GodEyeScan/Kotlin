package com.example.godeye.utils

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
import java.io.File

object VideoProcessor {

 private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

 /**
 * Procesa un video extrayendo frames y buscando placas
 * @param context Contexto de la aplicación
 * @param videoFile Archivo de video a procesar
 * @param onProgress Callback para reportar progreso (0.0 a 1.0)
 * @return Resultado con la mejor placa detectada y el texto completo
 */
 suspend fun processVideo(
 context: Context,
 videoFile: File,
 onProgress: (Float) -> Unit = {}
 ): VideoProcessingResult = withContext(Dispatchers.IO) {

 val retriever = MediaMetadataRetriever()
 val allDetectedTexts = mutableListOf<String>()
 val allDetectedPlates = mutableListOf<String>()

 try {
 retriever.setDataSource(videoFile.absolutePath)

 // Obtener duración del video en microsegundos
 val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
 val durationMicros = duration * 1000 // Convertir a microsegundos

 Log.d("VideoProcessor", "Duración del video: ${duration}ms")

 // Extraer frames cada 500ms (2 frames por segundo)
 val frameInterval = 500_000L // 500ms en microsegundos
 val totalFrames = (durationMicros / frameInterval).toInt()

 Log.d("VideoProcessor", "Extrayendo ~$totalFrames frames")

 var frameCount = 0
 var currentTime = 0L

 while (currentTime < durationMicros) {
 try {
 // Extraer frame en el tiempo actual
 val bitmap = retriever.getFrameAtTime(
 currentTime,
 MediaMetadataRetriever.OPTION_CLOSEST_SYNC
 )

 if (bitmap != null) {
 frameCount++

 // Reportar progreso
 val progress = currentTime.toFloat() / durationMicros.toFloat()
 onProgress(progress)

 Log.d("VideoProcessor", "Procesando frame $frameCount en ${currentTime / 1000}ms")

 // Procesar frame con OCR
 val image = InputImage.fromBitmap(bitmap, 0)
 val result = textRecognizer.process(image).await()
 val extractedText = result.text

                if (extractedText.isNotBlank()) {
                    Log.d("VideoProcessor", "Texto en frame $frameCount: $extractedText")
                    allDetectedTexts.add(extractedText)

                    // Intentar detectar placa en este frame
                    val plate = PlateDetector.detectPlate(extractedText)
                    if (plate != null) {
                        Log.d("VideoProcessor", "Placa detectada en frame $frameCount: $plate")
                        allDetectedPlates.add(plate)
                    }
                }

 bitmap.recycle()
 }

 } catch (e: Exception) {
 Log.e("VideoProcessor", "Error al procesar frame en ${currentTime / 1000}ms: ${e.message}")
 }

 currentTime += frameInterval
 }

 onProgress(1.0f)
 Log.d("VideoProcessor", "Procesamiento completado. Frames procesados: $frameCount")
 Log.d("VideoProcessor", "Placas detectadas: ${allDetectedPlates.size}")

 // Seleccionar la placa más común (la que aparece más veces)
 val bestPlate = allDetectedPlates
 .groupingBy { it }
 .eachCount()
 .maxByOrNull { it.value }
 ?.key

 Log.d("VideoProcessor", "Mejor placa detectada: $bestPlate")

 VideoProcessingResult(
 success = bestPlate != null,
 detectedPlate = bestPlate,
 allText = allDetectedTexts.joinToString("\n"),
 framesProcessed = frameCount,
 platesFound = allDetectedPlates.size
 )

 } catch (e: Exception) {
 Log.e("VideoProcessor", "Error al procesar video: ${e.message}", e)
 VideoProcessingResult(
 success = false,
 detectedPlate = null,
 allText = "",
 framesProcessed = 0,
 platesFound = 0,
 error = e.message
 )
 } finally {
 retriever.release()
 }
 }
}

/**
 * Resultado del procesamiento de video
 */
data class VideoProcessingResult(
 val success: Boolean,
 val detectedPlate: String?,
 val allText: String,
 val framesProcessed: Int,
 val platesFound: Int,
 val error: String? = null
)

