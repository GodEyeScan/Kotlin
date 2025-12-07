package com.example.godeye.ui

import android.app.Application
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.godeye.ui.auth.LoginScreen
import com.example.godeye.ui.theme.GodEyeTheme
import com.example.godeye.viewmodel.AuthViewModel
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

 private fun createAuthViewModel(): AuthViewModel {
 val context = InstrumentationRegistry.getInstrumentation().targetContext
 val application = context.applicationContext as Application
 return AuthViewModel(application)
 }

 @Test
 fun loginScreen_displaysAllComponents() {
 // Given
 val authViewModel = createAuthViewModel()

 composeTestRule.setContent {
 GodEyeTheme {
 LoginScreen(
 onLoginSuccess = {},
 onNavigateToRegister = {},
 authViewModel = authViewModel
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
 val authViewModel = createAuthViewModel()

 composeTestRule.setContent {
 GodEyeTheme {
 LoginScreen(
 onLoginSuccess = {},
 onNavigateToRegister = {},
 authViewModel = authViewModel
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
 val authViewModel = createAuthViewModel()

 composeTestRule.setContent {
 GodEyeTheme {
 LoginScreen(
 onLoginSuccess = {},
 onNavigateToRegister = {},
 authViewModel = authViewModel
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
 val authViewModel = createAuthViewModel()

 composeTestRule.setContent {
 GodEyeTheme {
 LoginScreen(
 onLoginSuccess = {},
 onNavigateToRegister = { registerClicked = true },
 authViewModel = authViewModel
 )
 }
 }

 // When
 composeTestRule.onNodeWithText("Regístrate").performClick()

 // Then
 assert(registerClicked)
 }
}

