package com.example.godeye.utils

import org.junit.Assert.*
import org.junit.Test

/**
 * ValidationUtilsTest
 *
 * Descripción: [Agregar descripción]
 *
 * @author GodEye Team
 * @version 1.0
 * @since 2025-12-07
 */
class ValidationUtilsTest {

 // ============ Pruebas de Validación de Nombre ============

 @Test
 fun `nombre valido debe retornar true`() {
 val result = ValidationUtils.isValidName("Juan Pérez")
 assertTrue(result.isValid)
 }

 @Test
 fun `nombre vacio debe retornar false`() {
 val result = ValidationUtils.isValidName("")
 assertFalse(result.isValid)
 assertTrue(result.message.contains("vacío"))
 }

 @Test
 fun `nombre con un caracter debe retornar false`() {
 val result = ValidationUtils.isValidName("A")
 assertFalse(result.isValid)
 assertTrue(result.message.contains("al menos 2"))
 }

 @Test
 fun `nombre muy largo debe retornar false`() {
 val nombreLargo = "A".repeat(51)
 val result = ValidationUtils.isValidName(nombreLargo)
 assertFalse(result.isValid)
 assertTrue(result.message.contains("largo"))
 }

 @Test
 fun `nombre con numeros debe retornar false`() {
 val result = ValidationUtils.isValidName("Juan123")
 assertFalse(result.isValid)
 assertTrue(result.message.contains("letras"))
 }

 @Test
 fun `nombre con caracteres especiales debe retornar false`() {
 val result = ValidationUtils.isValidName("Juan@Pérez")
 assertFalse(result.isValid)
 }

 @Test
 fun `nombre con tildes debe retornar true`() {
 val result = ValidationUtils.isValidName("José María")
 assertTrue(result.isValid)
 }

 @Test
 fun `nombre con ñ debe retornar true`() {
 val result = ValidationUtils.isValidName("Nuñez")
 assertTrue(result.isValid)
 }

 // ============ Pruebas de Validación de Email ============

 @Test
 fun `email valido debe retornar true`() {
 val result = ValidationUtils.isValidEmail("admin@gmail.com")
 assertTrue(result.isValid)
 }

 @Test
 fun `email sin arroba debe retornar false`() {
 val result = ValidationUtils.isValidEmail("admingmail.com")
 assertFalse(result.isValid)
 assertTrue(result.message.contains("@"))
 }

 @Test
 fun `email sin dominio debe retornar false`() {
 val result = ValidationUtils.isValidEmail("admin@")
 assertFalse(result.isValid)
 assertTrue(result.message.contains("formato"))
 }

 @Test
 fun `email sin punto debe retornar false`() {
 val result = ValidationUtils.isValidEmail("admin@gmailcom")
 assertFalse(result.isValid)
 }

 @Test
 fun `email vacio debe retornar false`() {
 val result = ValidationUtils.isValidEmail("")
 assertFalse(result.isValid)
 }

 @Test
 fun `email con formato complejo valido debe retornar true`() {
 val result = ValidationUtils.isValidEmail("usuario.nombre+tag@dominio.co.uk")
 assertTrue(result.isValid)
 }

 // ============ Pruebas de Validación de Contraseña ============

 @Test
 fun `contraseña valida debe retornar true`() {
 val result = ValidationUtils.isValidPassword("123456")
 assertTrue(result.isValid)
 }

 @Test
 fun `contraseña de 6 caracteres debe retornar true`() {
 val result = ValidationUtils.isValidPassword("abcd12")
 assertTrue(result.isValid)
 }

 @Test
 fun `contraseña menor a 6 caracteres debe retornar false`() {
 val result = ValidationUtils.isValidPassword("12345")
 assertFalse(result.isValid)
 assertTrue(result.message.contains("6"))
 }

 @Test
 fun `contraseña vacia debe retornar false`() {
 val result = ValidationUtils.isValidPassword("")
 assertFalse(result.isValid)
 }

 @Test
 fun `contraseña muy larga debe retornar false`() {
 val passwordLarga = "A".repeat(51)
 val result = ValidationUtils.isValidPassword(passwordLarga)
 assertFalse(result.isValid)
 }

 @Test
 fun `contraseña segura con caracteres especiales debe retornar true`() {
 val result = ValidationUtils.isValidPassword("Pass@123")
 assertTrue(result.isValid)
 }

 // ============ Pruebas de Validación de Número de Celular ============

 @Test
 fun `numero valido debe retornar true`() {
 val result = ValidationUtils.isValidPhoneNumber("3001234567")
 assertTrue(result.isValid)
 }

 @Test
 fun `numero de 7 digitos debe retornar true`() {
 val result = ValidationUtils.isValidPhoneNumber("1234567")
 assertTrue(result.isValid)
 }

 @Test
 fun `numero con letras debe retornar false`() {
 val result = ValidationUtils.isValidPhoneNumber("300ABC1234")
 assertFalse(result.isValid)
 assertTrue(result.message.contains("dígitos"))
 }

 @Test
 fun `numero muy corto debe retornar false`() {
 val result = ValidationUtils.isValidPhoneNumber("123456")
 assertFalse(result.isValid)
 assertTrue(result.message.contains("7"))
 }

 @Test
 fun `numero muy largo debe retornar false`() {
 val result = ValidationUtils.isValidPhoneNumber("1234567890123456")
 assertFalse(result.isValid)
 assertTrue(result.message.contains("largo"))
 }

 @Test
 fun `numero vacio debe retornar false`() {
 val result = ValidationUtils.isValidPhoneNumber("")
 assertFalse(result.isValid)
 }

 @Test
 fun `numero con espacios debe retornar false`() {
 val result = ValidationUtils.isValidPhoneNumber("300 123 4567")
 assertFalse(result.isValid)
 }

 // ============ Pruebas de Validación de NIT ============

 @Test
 fun `NIT valido debe retornar true`() {
 val result = ValidationUtils.isValidNIT("123456789-0")
 assertTrue(result.isValid)
 }

 @Test
 fun `NIT sin guiones debe retornar true`() {
 val result = ValidationUtils.isValidNIT("1234567890")
 assertTrue(result.isValid)
 }

 @Test
 fun `NIT muy corto debe retornar false`() {
 val result = ValidationUtils.isValidNIT("1234")
 assertFalse(result.isValid)
 assertTrue(result.message.contains("5"))
 }

 @Test
 fun `NIT con letras debe retornar false`() {
 val result = ValidationUtils.isValidNIT("12345ABC")
 assertFalse(result.isValid)
 }

 @Test
 fun `NIT vacio debe retornar false`() {
 val result = ValidationUtils.isValidNIT("")
 assertFalse(result.isValid)
 }

 // ============ Pruebas de Prefijos Telefónicos ============

 @Test
 fun `lista de prefijos no debe estar vacia`() {
 val prefixes = ValidationUtils.getPhonePrefixes()
 assertTrue(prefixes.isNotEmpty())
 }

 @Test
 fun `lista de prefijos debe contener Colombia`() {
 val prefixes = ValidationUtils.getPhonePrefixes()
 assertTrue(prefixes.contains("+57"))
 }

 @Test
 fun `lista de prefijos debe contener Venezuela`() {
 val prefixes = ValidationUtils.getPhonePrefixes()
 assertTrue(prefixes.contains("+58"))
 }
}

