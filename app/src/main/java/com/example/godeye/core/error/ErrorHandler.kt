package com.example.godeye.core.error

import timber.log.Timber

/**
 * Sealed /**
 * para
 *
 * Descripción: [Agregar descripción]
 *
 * @author GodEye Team
 * @version 1.0
 * @since 2025-12-07
 */
class para representar diferentes tipos de errores en la aplicación
 * Implementa el patrón Result para manejo de errores tipado y seguro
 */
sealed class AppError(open val message: String) {

 // Errores de red/conectividad
 data class NetworkError(override val message: String = "Error de conexión a internet") : AppError(message)

 // Errores de base de datos
 data class DatabaseError(override val message: String = "Error al acceder a la base de datos") : AppError(message)

 // Errores de permisos
 data class PermissionError(override val message: String = "Permiso denegado") : AppError(message)

 // Errores de OCR/ML Kit
 data class OcrError(override val message: String = "Error al procesar imagen") : AppError(message)

 // Errores de cámara
 data class CameraError(override val message: String = "Error al acceder a la cámara") : AppError(message)

 // Errores de ubicación/GPS
 data class LocationError(override val message: String = "Error al obtener ubicación") : AppError(message)

 // Errores de validación
 data class ValidationError(override val message: String = "Datos inválidos") : AppError(message)

 // Errores de autenticación
 data class AuthenticationError(override val message: String = "Error de autenticación") : AppError(message)

 // Error genérico
 data class UnknownError(override val message: String = "Error desconocido", val throwable: Throwable? = null) : AppError(message)
}

/**
 * Extension function para convertir excepciones a AppError
 */
fun Throwable.toAppError(): AppError {
 Timber.e(this, "Convirtiendo excepción a AppError")

 return when (this) {
 is java.net.UnknownHostException,
 is java.net.SocketTimeoutException,
 is java.io.IOException -> AppError.NetworkError(message ?: "Error de conexión")

 is android.database.sqlite.SQLiteException -> AppError.DatabaseError(message ?: "Error de base de datos")

 is SecurityException -> AppError.PermissionError(message ?: "Permiso denegado")

 is IllegalArgumentException,
 is IllegalStateException -> AppError.ValidationError(message ?: "Validación fallida")

 else -> AppError.UnknownError(message ?: "Error inesperado", this)
 }
}

/**
 * Clase para manejar estados de UI con loading, success y error
 * Implementa el patrón State para gestión de estados de interfaz
 */
sealed class UiState<out T> {
 object Idle : UiState<Nothing>()
 object Loading : UiState<Nothing>()
 data class Success<T>(val data: T) : UiState<T>()
 data class Error(val error: AppError) : UiState<Nothing>()
}

/**
 * Extension functions para facilitar el uso de UiState
 */
fun <T> UiState<T>.isLoading(): Boolean = this is UiState.Loading
fun <T> UiState<T>.isSuccess(): Boolean = this is UiState.Success
fun <T> UiState<T>.isError(): Boolean = this is UiState.Error
fun <T> UiState<T>.isIdle(): Boolean = this is UiState.Idle

fun <T> UiState<T>.getDataOrNull(): T? = (this as? UiState.Success)?.data
fun <T> UiState<T>.getErrorOrNull(): AppError? = (this as? UiState.Error)?.error

/**
 * Helper para ejecutar operaciones con manejo de errores automático
 */
suspend fun <T> safeCall(
 onError: ((AppError) -> Unit)? = null,
 block: suspend () -> T
): Result<T> {
 return try {
 Result.success(block())
 } catch (e: Exception) {
 Timber.e(e, "SafeCall: Capturada excepción")
 val appError = e.toAppError()
 onError?.invoke(appError)
 Result.failure(e)
 }
}

/**
 * Extension function para Result que convierte a UiState
 */
fun <T> Result<T>.toUiState(): UiState<T> {
 return fold(
 onSuccess = { UiState.Success(it) },
 onFailure = { UiState.Error((it as? Exception)?.toAppError() ?: AppError.UnknownError(it.message ?: "Error", it)) }
 )
}

