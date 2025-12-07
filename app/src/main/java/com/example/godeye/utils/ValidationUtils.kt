package com.example.godeye.utils

object ValidationUtils {

 /**
 * Valida que el nombre tenga sentido
 * - Mínimo 2 caracteres
 * - Máximo 50 caracteres
 * - Solo letras y espacios
 * - No puede estar vacío
 */
 fun isValidName(name: String): ValidationResult {
 return when {
 name.isBlank() -> ValidationResult(false, "El nombre no puede estar vacío")
 name.length < 2 -> ValidationResult(false, "El nombre debe tener al menos 2 caracteres")
 name.length > 50 -> ValidationResult(false, "El nombre es demasiado largo (máximo 50 caracteres)")
 !name.matches(Regex("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$")) ->
 ValidationResult(false, "El nombre solo puede contener letras y espacios")
 else -> ValidationResult(true, "Nombre válido")
 }
 }

 /**
 * Valida que el email sea válido
 * - Debe contener @
 * - Debe tener dominio
 * - Formato válido
 */
 fun isValidEmail(email: String): ValidationResult {
 return when {
 email.isBlank() -> ValidationResult(false, "El email no puede estar vacío")
 !email.contains("@") -> ValidationResult(false, "El email debe contener @")
 !email.matches(Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) ->
 ValidationResult(false, "El email no tiene un formato válido")
 else -> ValidationResult(true, "Email válido")
 }
 }

 /**
 * Valida que la contraseña sea segura
 * - Mínimo 6 caracteres
 * - No puede estar vacía
 */
 fun isValidPassword(password: String): ValidationResult {
 return when {
 password.isBlank() -> ValidationResult(false, "La contraseña no puede estar vacía")
 password.length < 6 -> ValidationResult(false, "La contraseña debe tener al menos 6 caracteres")
 password.length > 50 -> ValidationResult(false, "La contraseña es demasiado larga")
 else -> ValidationResult(true, "Contraseña válida")
 }
 }

 /**
 * Valida que el número de celular tenga sentido
 * - Solo números
 * - Longitud entre 7 y 15 dígitos
 */
 fun isValidPhoneNumber(phoneNumber: String): ValidationResult {
 return when {
 phoneNumber.isBlank() -> ValidationResult(false, "El número no puede estar vacío")
 !phoneNumber.matches(Regex("^[0-9]+$")) ->
 ValidationResult(false, "El número solo puede contener dígitos")
 phoneNumber.length < 7 -> ValidationResult(false, "El número debe tener al menos 7 dígitos")
 phoneNumber.length > 15 -> ValidationResult(false, "El número es demasiado largo (máximo 15 dígitos)")
 else -> ValidationResult(true, "Número válido")
 }
 }

 /**
 * Valida el NIT
 * - Mínimo 5 caracteres
 * - Solo números y guiones
 */
 fun isValidNIT(nit: String): ValidationResult {
 return when {
 nit.isBlank() -> ValidationResult(false, "El NIT no puede estar vacío")
 !nit.matches(Regex("^[0-9-]+$")) ->
 ValidationResult(false, "El NIT solo puede contener números y guiones")
 nit.replace("-", "").length < 5 ->
 ValidationResult(false, "El NIT debe tener al menos 5 dígitos")
 else -> ValidationResult(true, "NIT válido")
 }
 }

 /**
 * Obtiene lista de prefijos telefónicos comunes
 */
 fun getPhonePrefixes(): List<String> {
 return listOf(
 "+57", // Colombia
 "+58", // Venezuela
 "+1", // USA/Canada
 "+52", // México
 "+54", // Argentina
 "+55", // Brasil
 "+56", // Chile
 "+51", // Perú
 "+593", // Ecuador
 "+507", // Panamá
 "+34", // España
 "+44", // Reino Unido
 )
 }
}

/**
 * ValidationResult
 *
 * Descripción: [Agregar descripción]
 *
 * @author GodEye Team
 * @version 1.0
 * @since 2025-12-07
 */
data class ValidationResult(
 val isValid: Boolean,
 val message: String
)

