package com.example.godeye.data.api

import com.google.gson.annotations.SerializedName

/**
 * Modelo de solicitud para registro de usuario
 *
 * Endpoint: POST /auth/register
 * URL: https://gateway.helmer-pardo.com/auth/register
 */
data class RegisterRequest(
 @SerializedName("email")
 val email: String,

 @SerializedName("password")
 val password: String
)

/**
 * Modelo de respuesta de registro/login
 */
data class AuthResponse(
 @SerializedName("token")
 val token: String? = null,

 @SerializedName("user")
 val user: UserResponse? = null,

 @SerializedName("message")
 val message: String? = null,

 @SerializedName("access_token")
 val accessToken: String? = null
) {
 // Método helper para obtener el token en cualquier formato
 fun extractToken(): String? {
 return token ?: accessToken
 }
}

/**
 * Modelo de usuario en la respuesta
 */
data class UserResponse(
 @SerializedName("id")
 val id: String? = null,

 @SerializedName("email")
 val email: String,

 @SerializedName("createdAt")
 val createdAt: String? = null,

 @SerializedName("updatedAt")
 val updatedAt: String? = null
)

