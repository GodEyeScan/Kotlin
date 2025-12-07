package com.example.godeye.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Unit Tests adicionales para ValidationUtils
 * Prueba la lógica de validación de datos de usuario usando Truth
 */
class ValidationUtilsExtraTest {

 @Test
 fun `isValidEmail should return true for valid email`() {
 // Given
 val validEmails = listOf(
 "user@example.com",
 "test.user@domain.co.uk",
 "name123@test.org"
 )

 // When & Then
 validEmails.forEach { email ->
 val result = ValidationUtils.isValidEmail(email)
 assertThat(result.isValid).isTrue()
 }
 }

 @Test
 fun `isValidEmail should return false for invalid email`() {
 // Given
 val invalidEmails = listOf(
 "",
 "invalid",
 "@example.com",
 "user@",
 "user @example.com"
 )

 // When & Then
 invalidEmails.forEach { email ->
 val result = ValidationUtils.isValidEmail(email)
 assertThat(result.isValid).isFalse()
 }
 }

 @Test
 fun `isValidPassword should return true for password with 6 or more characters`() {
 // Given
 val validPasswords = listOf(
 "123456",
 "password",
 "MyP@ssw0rd"
 )

 // When & Then
 validPasswords.forEach { password ->
 val result = ValidationUtils.isValidPassword(password)
 assertThat(result.isValid).isTrue()
 }
 }

 @Test
 fun `isValidPassword should return false for password with less than 6 characters`() {
 // Given
 val invalidPasswords = listOf(
 "",
 "12345",
 "pass"
 )

 // When & Then
 invalidPasswords.forEach { password ->
 val result = ValidationUtils.isValidPassword(password)
 assertThat(result.isValid).isFalse()
 }
 }
}

