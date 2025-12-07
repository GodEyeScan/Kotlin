package com.example.godeye.data.repository

import com.example.godeye.data.api.*
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

/**
 * Repositorio para manejar la autenticación con la API en la nube
 *
 * Endpoints:
 * - POST /auth/register - Registrar nuevo usuario
 * - POST /auth/login - Iniciar sesión (si existe)
 */
class AuthRepository {

 private val apiService = RetrofitClient.apiService
 private val gson = Gson()

 /**
 * Registra un nuevo usuario en el sistema
 *
 * @param email Email del usuario
 * @param password Contraseña del usuario
 * @return ApiResult con el token de autenticación o error
 */
 suspend fun register(email: String, password: String): ApiResult<AuthResponse> {
 return withContext(Dispatchers.IO) {
 try {
 val request = RegisterRequest(email, password)
 val response = apiService.register(request)
 handleAuthResponse(response)
 } catch (e: Exception) {
 ApiResult.Error("Error de conexión: ${e.localizedMessage}")
 }
 }
 }

 /**
 * Registra un nuevo administrador en el sistema
 *
 * Este método crea un usuario con rol administrativo que tendrá
 * acceso a endpoints de administración como /admin/reports
 *
 * @param email Email del administrador
 * @param password Contraseña del administrador
 * @return ApiResult con el token de autenticación o error
 */
 suspend fun registerAdmin(email: String, password: String): ApiResult<AuthResponse> {
 return withContext(Dispatchers.IO) {
 try {
 val request = RegisterRequest(email, password)
 val response = apiService.registerAdmin(request)
 handleAuthResponse(response)
 } catch (e: Exception) {
 ApiResult.Error("Error de conexión: ${e.localizedMessage}")
 }
 }
 }

 /**
 * Inicia sesión con un usuario existente
 *
 * @param email Email del usuario
 * @param password Contraseña del usuario
 * @return ApiResult con el token de autenticación o error
 */
 suspend fun login(email: String, password: String): ApiResult<AuthResponse> {
 return withContext(Dispatchers.IO) {
 try {
 val request = RegisterRequest(email, password)
 val response = apiService.login(request)
 handleAuthResponse(response)
 } catch (e: Exception) {
 ApiResult.Error("Error de conexión: ${e.localizedMessage}")
 }
 }
 }

 /**
 * Maneja la respuesta de autenticación
 */
 private fun handleAuthResponse(response: Response<AuthResponse>): ApiResult<AuthResponse> {
 return if (response.isSuccessful) {
 val authResponse = response.body()
 if (authResponse != null && authResponse.extractToken() != null) {
 ApiResult.Success(authResponse)
 } else {
 ApiResult.Error("Respuesta sin token de autenticación")
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
}

/**
 * Clase sellada para representar el resultado de las operaciones de API
 */
sealed class ApiResult<out T> {
 data class Success<T>(val data: T) : ApiResult<T>()
 data class Error(val message: String) : ApiResult<Nothing>()

 /**
 * Helper para ejecutar código solo si el resultado es exitoso
 */
 inline fun onSuccess(action: (T) -> Unit): ApiResult<T> {
 if (this is Success) {
 action(data)
 }
 return this
 }

 /**
 * Helper para ejecutar código solo si el resultado es error
 */
 inline fun onError(action: (String) -> Unit): ApiResult<T> {
 if (this is Error) {
 action(message)
 }
 return this
 }
}

