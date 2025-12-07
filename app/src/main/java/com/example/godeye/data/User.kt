package com.example.godeye.data

/**
 * User
 *
 * Descripción: [Agregar descripción]
 *
 * @author GodEye Team
 * @version 1.0
 * @since 2025-12-07
 */
data class User(
 val email: String,
 val password: String,
 val name: String = "",
 val phonePrefix: String = "+57",
 val phoneNumber: String = "",
 val nit: String = "",
 val userType: UserType = UserType.NORMAL,
 var profilePhotoUri: String = "" // URI de la foto de perfil
)

enum class UserType {
 ADMIN, // admin@gmail.com - Acceso normal
 DEVELOPER, // dev@gmail.com - Ve texto OCR
 NORMAL // Usuarios registrados - Acceso normal
}

