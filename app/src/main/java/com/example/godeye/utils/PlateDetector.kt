package com.example.godeye.utils

import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await
import java.io.File

object PlateDetector {

 private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

 // Patrones comunes de placas (ajusta según tu región)
 // Patrones más flexibles para detectar placas colombianas y otros formatos
 private val platePatterns = listOf(
 // Formatos con espacios o guiones
 Regex("[A-Z]{3}\\s*[0-9]{3}"), // ABC123, ABC 123
 Regex("[A-Z]{3}-?[0-9]{3}"), // ABC-123, ABC123
 Regex("[A-Z]{3}\\s+[0-9]{3}"), // ABC 123 (espacios múltiples)

 // Formatos invertidos
 Regex("[0-9]{3}\\s*[A-Z]{3}"), // 123ABC, 123 ABC
 Regex("[0-9]{3}-?[A-Z]{3}"), // 123-ABC, 123ABC

 // Formatos con 2 letras
 Regex("[A-Z]{2}\\s*[0-9]{4}"), // AB1234, AB 1234
 Regex("[A-Z]{2}-?[0-9]{4}"), // AB-1234

 // Formatos mixtos
 Regex("[A-Z]{3}\\s*[0-9]{2}\\s*[A-Z]{1}"), // ABC12D, ABC 12 D
 Regex("[A-Z]{1}\\s*[0-9]{2}\\s*[A-Z]{3}"), // A12BCD, A 12 BCD
 Regex("[A-Z]{2}-?[0-9]{2}-?[0-9]{2}"), // AB-12-34

 // Formatos de 4 letras + 3 números (algunos países)
 Regex("[A-Z]{4}\\s*[0-9]{3}"), // ABCD123

 // Formato flexible: cualquier combinación de 2-4 letras seguidas de 2-4 números
 Regex("[A-Z]{2,4}\\s*[0-9]{2,4}"), // Flexible
 Regex("[0-9]{2,4}\\s*[A-Z]{2,4}") // Invertido flexible
 )

 /**
 * Procesa una imagen y extrae texto usando ML Kit OCR
 * @param imageUri URI de la imagen a procesar
 * @return Texto extraído de la imagen
 */
 suspend fun extractTextFromImage(imageUri: String): String {
 return try {
 val file = File(Uri.parse(imageUri).path ?: return "")
 if (!file.exists()) {
 Log.e("PlateDetector", "Archivo no existe: ${file.absolutePath}")
 return ""
 }

 val bitmap = BitmapFactory.decodeFile(file.absolutePath)
 if (bitmap == null) {
 Log.e("PlateDetector", "No se pudo decodificar el bitmap")
 return ""
 }

 val image = InputImage.fromBitmap(bitmap, 0)
 val result = textRecognizer.process(image).await()

 val extractedText = result.text
 Log.d("PlateDetector", "Texto extraído: $extractedText")

 bitmap.recycle()
 extractedText

 } catch (e: Exception) {
 Log.e("PlateDetector", "Error al extraer texto: ${e.message}", e)
 ""
 }
 }

 /**
 * Detecta si el texto contiene una posible placa vehicular
 * @param text Texto extraído de la imagen
 * @return Placa detectada o null si no se encuentra
 */
 fun detectPlate(text: String): String? {
 if (text.isBlank()) return null

 // Lista de palabras a ignorar (ciudades, palabras comunes, etc.)
 val ignoredWords = setOf(
 "MEDELLIN", "BOGOTA", "CALI", "BARRANQUILLA", "CARTAGENA",
 "ESTILOS", "IMAGEN", "DE", "LA", "EL", "LOS", "LAS",
 "COLOMBIA", "VEHICULO", "PLACA", "TRANSITO",
 "MINISTERIO", "TRANSPORTE", "DEPARTAMENTO",
 "POLICIA", "NACIONAL", "PUBLICO", "PARTICULAR"
 )

 // Limpiar el texto
 val cleanedText = text.trim()
 .replace("\\s+".toRegex(), " ")
 .uppercase()

 Log.d("PlateDetector", "Texto limpio para análisis: $cleanedText")

 // Dividir en líneas y palabras
 val lines = cleanedText.split("\n")
 val allWords = cleanedText.split(Regex("\\s+|[^A-Z0-9]+"))

        // Buscar secuencias de letras y números que parezcan placas
        for (word in allWords) {
            // Ignorar palabras en la lista de ignorados
            if (ignoredWords.contains(word)) {
                Log.d("PlateDetector", "→ Palabra ignorada: $word")
                continue
            }

            // Ignorar palabras muy cortas o muy largas
            if (word.length < 5 || word.length > 10) {
                continue
            }

            // Contar letras y números
            val letterCount = word.count { it.isLetter() }
            val digitCount = word.count { it.isDigit() }

            // Una placa típica tiene 3 letras y 3 números, o 2 letras y 4 números
            // Debe tener al menos 2 letras y 2 números
            if (letterCount >= 2 && digitCount >= 2) {
                // Verificar que no tenga caracteres extraños
                if (word.all { it.isLetterOrDigit() }) {
                    Log.d("PlateDetector", "Placa detectada: $word")
 return word
 }
 }
 }

 // Buscar patrones específicos en cada línea
 for (line in lines) {
 val cleanLine = line.trim()
 .replace("-", "")
 .replace(" ", "")
 .filter { it.isLetterOrDigit() }

 // Ignorar líneas con palabras ignoradas
 var hasIgnoredWord = false
 for (ignored in ignoredWords) {
 if (cleanLine.contains(ignored)) {
 hasIgnoredWord = true
 break
 }
 }

            if (hasIgnoredWord) {
                Log.d("PlateDetector", "→ Línea ignorada por palabra prohibida: $cleanLine")
                continue
            }

            // Ignorar líneas muy largas o muy cortas
            if (cleanLine.length < 5 || cleanLine.length > 10) {
                continue
            }

            // Verificar patrón de placa
            val letterCount = cleanLine.count { it.isLetter() }
            val digitCount = cleanLine.count { it.isDigit() }

            if (letterCount >= 2 && digitCount >= 2) {
                Log.d("PlateDetector", "Placa detectada en línea: $cleanLine")
 return cleanLine
 }
 }

 // Buscar con regex más estrictos
 for (pattern in platePatterns) {
 val match = pattern.find(cleanedText)
 if (match != null) {
 val candidate = match.value.replace("-", "").replace(" ", "")

 // Verificar que no contenga palabras ignoradas
 var isValid = true
 for (ignored in ignoredWords) {
 if (candidate.contains(ignored)) {
 isValid = false
 break
 }
 }

                if (isValid && candidate.length >= 5 && candidate.length <= 10) {
                    Log.d("PlateDetector", "Placa detectada por patrón: $candidate")
                    return candidate
                }
            }
        }

        Log.d("PlateDetector", "No se detectó placa en el texto")
        return null
 }

 /**
 * Valida si una captura debe guardarse según el texto extraído
 * @param text Texto extraído de la imagen
 * @return true si debe guardarse, false si debe ignorarse
 */
 fun shouldSaveCapture(text: String): Boolean {
 // Si no hay texto, ignorar
 if (text.isBlank()) {
 Log.d("PlateDetector", "Captura ignorada: sin texto")
 return false
 }

 Log.d("PlateDetector", "Evaluando texto (${text.length} caracteres): $text")

 // Si hay demasiado texto (más de 300 caracteres), probablemente no es una placa
 // (aumentado de 200 a 300 para ser más permisivo)
 if (text.length > 300) {
 Log.d("PlateDetector", "Captura ignorada: demasiado texto (${text.length} caracteres)")
 return false
 }

 // Intentar detectar placa
 val plate = detectPlate(text)

        // Solo guardar si se detectó una placa
        val shouldSave = plate != null
        if (shouldSave) {
            Log.d("PlateDetector", "Captura válida: placa detectada '$plate'")
        } else {
            Log.d("PlateDetector", "Captura ignorada: no se detectó placa")
            Log.d("PlateDetector", "Texto completo analizado: $text")
 }

 return shouldSave
 }

 /**
 * Procesa una imagen completa: extrae texto y detecta placa
 * @param imageUri URI de la imagen
 * @return Pair<texto extraído, placa detectada o null>
 */
 suspend fun processImage(imageUri: String): Pair<String, String?> {
 val text = extractTextFromImage(imageUri)
 val plate = detectPlate(text)
 return Pair(text, plate)
 }

 /**
 * Limpia recursos del detector de texto
 */
 fun cleanup() {
 textRecognizer.close()
 }
}

