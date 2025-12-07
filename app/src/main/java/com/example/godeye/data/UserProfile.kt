package com.example.godeye.data

/**
 * Modelo de datos para el perfil del usuario
 * Estos datos se guardan LOCALMENTE y no se sincronizan con la nube
 */
data class UserProfile(
 val email: String,
 val name: String = "",
 val phone: String = "",
 val nit: String = ""
)

