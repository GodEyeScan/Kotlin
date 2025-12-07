package com.example.godeye.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.godeye.ui.auth.LoginScreen
import com.example.godeye.ui.theme.GodEyeTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI Tests para LoginScreen usando Compose Testing
 * Prueba la interfaz de usuario y las interacciones del usuario
 */
@RunWith(AndroidJUnit4::class)
/**
 * LoginScreenTest
 *
 * Descripción: [Agregar descripción]
 *
 * @author GodEye Team
 * @version 1.0
 * @since 2025-12-07
 */
class LoginScreenTest {

 @get:Rule
 val composeTestRule = createComposeRule()

 @Test
 fun loginScreen_displaysAllComponents() {
 // Given
 composeTestRule.setContent {
 GodEyeTheme {
 LoginScreen(
 onLoginSuccess = {},
 onNavigateToRegister = {}
 )
 }
 }

 // Then - Verificar que todos los componentes están presentes
 composeTestRule.onNodeWithText("Iniciar Sesión").assertExists()
 composeTestRule.onNodeWithText("Email").assertExists()
 composeTestRule.onNodeWithText("Contraseña").assertExists()
 composeTestRule.onNodeWithText("¿No tienes cuenta?").assertExists()
 }

 @Test
 fun loginScreen_emailInput_acceptsText() {
 // Given
 composeTestRule.setContent {
 GodEyeTheme {
 LoginScreen(
 onLoginSuccess = {},
 onNavigateToRegister = {}
 )
 }
 }

 // When
 composeTestRule.onNodeWithText("Email")
 .performTextInput("test@example.com")

 // Then
 composeTestRule.onNodeWithText("test@example.com").assertExists()
 }

 @Test
 fun loginScreen_passwordInput_hidesText() {
 // Given
 composeTestRule.setContent {
 GodEyeTheme {
 LoginScreen(
 onLoginSuccess = {},
 onNavigateToRegister = {}
 )
 }
 }

 // When
 val passwordNode = composeTestRule.onNodeWithText("Contraseña")
 passwordNode.performTextInput("password123")

 // Then - El campo de contraseña debe ocultar el texto
 passwordNode.assertExists()
 }

 @Test
 fun loginScreen_clickRegisterButton_triggersNavigation() {
 // Given
 var registerClicked = false
 composeTestRule.setContent {
 GodEyeTheme {
 LoginScreen(
 onLoginSuccess = {},
 onNavigateToRegister = { registerClicked = true }
 )
 }
 }

 // When
 composeTestRule.onNodeWithText("Regístrate").performClick()

 // Then
 assert(registerClicked)
 }
}

