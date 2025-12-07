/**
 * AuthViewModel.kt
 *
 * Maneja la lógica de autenticación (registro, login, logout) y el estado del usuario.
 */

package com.example.godeye.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.godeye.data.User
import com.example.godeye.data.UserType
import com.example.godeye.data.UserProfile
import com.example.godeye.data.repository.AuthRepository
import com.example.godeye.data.repository.UserProfileRepository
import com.example.godeye.data.repository.ApiResult
import androidx.compose.runtime.State
import kotlinx.coroutines.launch

/**
 * AuthViewModel
 *
 * Descripción: [Agregar descripción]
 *
 * @author GodEye Team
 * @version 1.0
 * @since 2025-12-07
 */
class AuthViewModel(application: Application) : AndroidViewModel(application) {

 private val authRepository = AuthRepository()
 private val userProfileRepository = UserProfileRepository(application)

 // Estado de autenticación
 private val _isAuthenticated = mutableStateOf(false)
 val isAuthenticated: State<Boolean> = _isAuthenticated

 private val _currentUser = mutableStateOf<User?>(null)
 val currentUser: State<User?> = _currentUser

 // Perfil del usuario (datos locales)
 private val _userProfile = mutableStateOf<UserProfile?>(null)
 val userProfile: State<UserProfile?> = _userProfile

 // Token de autenticación de la API
 private val _accessToken = mutableStateOf<String?>(null)
 val accessToken: State<String?> = _accessToken

 // Estado de carga
 private val _isLoading = mutableStateOf(false)
 val isLoading: State<Boolean> = _isLoading

 // Indica si el usuario actual es administrador
 private val _isAdmin = mutableStateOf(false)
 val isAdmin: State<Boolean> = _isAdmin

 /**
 * Intenta iniciar sesión con email y contraseña
 * Conecta con la API en la nube
 */
 fun login(email: String, password: String, onResult: (LoginResult) -> Unit) {
 viewModelScope.launch {
 _isLoading.value = true

 // Detectar si es admin
 val isAdminUser = email.lowercase() == "admin@gmail.com"
 _isAdmin.value = isAdminUser

 when (val result = authRepository.login(email, password)) {
 is ApiResult.Success -> {
 val token = result.data.extractToken()
 if (token != null) {
 _accessToken.value = token
 _isAuthenticated.value = true

 // Crear usuario local con la info de la API
 val userType = if (isAdminUser) UserType.DEVELOPER else UserType.NORMAL
 val user = User(
 email = result.data.user?.email ?: email,
 password = password,
 name = email.substringBefore("@"),
 userType = userType
 )
 _currentUser.value = user

 _isLoading.value = false
 onResult(LoginResult.Success(user))

 // Cargar perfil local
 loadUserProfile(email)
 } else {
 _isLoading.value = false
 onResult(LoginResult.Error("Token de autenticación no recibido"))
 }
 }
 is ApiResult.Error -> {
 _isLoading.value = false
 onResult(LoginResult.Error(result.message))
 }
 }
 }
 }

 /**
 * Carga el perfil del usuario desde la base de datos local
 * y actualiza currentUser con esos datos si existen
 */
 private fun loadUserProfile(email: String) {
 viewModelScope.launch {
 val profile = userProfileRepository.getProfile(email)
 if (profile != null) {
 _userProfile.value = profile

 // Actualizar currentUser con los datos del perfil
 _currentUser.value?.let { currentUser ->
 val updatedUser = currentUser.copy(
 name = profile.name,
 phonePrefix = profile.phone.substringBefore(" "),
 phoneNumber = profile.phone.substringAfter(" "),
 nit = profile.nit
 )
 _currentUser.value = updatedUser
 }

            android.util.Log.i("AuthViewModel", "Perfil cargado desde BD local: ${profile.name}")
        } else {
            _userProfile.value = UserProfile(email = email)
            android.util.Log.w("AuthViewModel", "No hay perfil local guardado para: $email")
 }
 }
 }

 /**
 * Guarda el perfil del usuario en la base de datos local
 * Estos datos (nombre, teléfono, NIT) NO se sincronizan con la nube
 */
 fun saveUserProfile(name: String, phone: String, nit: String) {
 viewModelScope.launch {
 val email = _currentUser.value?.email ?: return@launch
 val profile = UserProfile(
 email = email,
 name = name,
 phone = phone,
 nit = nit
 )
 userProfileRepository.saveProfile(profile)
 _userProfile.value = profile
 }
 }

 /**
 * Registra un nuevo usuario en la API en la nube
 */
 fun register(user: User, onResult: (RegisterResult) -> Unit) {
 viewModelScope.launch {
 _isLoading.value = true

 when (val result = authRepository.register(user.email, user.password)) {
 is ApiResult.Success -> {
 val token = result.data.extractToken()
 if (token != null) {
 _accessToken.value = token
 _isAuthenticated.value = true
 _currentUser.value = user

 // Guardar perfil local con los datos del registro
 val profile = UserProfile(
 email = user.email,
 name = user.name,
 phone = "${user.phonePrefix} ${user.phoneNumber}",
 nit = user.nit
 )
 userProfileRepository.saveProfile(profile)
 _userProfile.value = profile

 _isLoading.value = false
 onResult(RegisterResult.Success)
 } else {
 _isLoading.value = false
 onResult(RegisterResult.Error("Token de autenticación no recibido"))
 }
 }
 is ApiResult.Error -> {
 _isLoading.value = false
 onResult(RegisterResult.Error(result.message))
 }
 }
 }
 }

 /**
 * Cierra la sesión actual
 */
 fun logout() {
 _isAuthenticated.value = false
 _currentUser.value = null
 _accessToken.value = null
 _isAdmin.value = false
 _userProfile.value = null
 }
}

sealed class LoginResult {
 data class Success(val user: User) : LoginResult()
 data class Error(val message: String) : LoginResult()
}

sealed class RegisterResult {
 object Success : RegisterResult()
 data class Error(val message: String) : RegisterResult()
}
