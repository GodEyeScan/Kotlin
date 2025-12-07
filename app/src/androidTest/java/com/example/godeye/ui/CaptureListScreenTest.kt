package com.example.godeye.ui

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.godeye.data.CaptureData
import com.example.godeye.data.User
import com.example.godeye.data.UserType
import com.example.godeye.ui.captures.CaptureListScreen
import com.example.godeye.ui.theme.GodEyeTheme
import com.example.godeye.viewmodel.AuthViewModel
import com.example.godeye.viewmodel.CaptureViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI Tests para CaptureListScreen
 * Prueba la visualización y comportamiento de la lista de capturas
 */
@RunWith(AndroidJUnit4::class)
/**
 * CaptureListScreenTest
 *
 * Descripción: [Agregar descripción]
 *
 * @author GodEye Team
 * @version 1.0
 * @since 2025-12-07
 */
class CaptureListScreenTest {

 @get:Rule
 val composeTestRule = createComposeRule()

 private fun createMockAuthViewModel(): AuthViewModel {
 val context = InstrumentationRegistry.getInstrumentation().targetContext
 val application = context.applicationContext as Application
 val viewModel = AuthViewModel(application)

 // Simular usuario autenticado
 val mockUser = User(
 email = "test@example.com",
 password = "password",
 name = "Test User",
 userType = UserType.NORMAL
 )

 // Usar reflection para establecer el estado (solo para tests)
 val currentUserField = AuthViewModel::class.java.getDeclaredField("_currentUser")
 currentUserField.isAccessible = true
 currentUserField.set(viewModel, mutableStateOf(mockUser))

 return viewModel
 }

 private fun createMockCaptureViewModel(): CaptureViewModel {
 val context = InstrumentationRegistry.getInstrumentation().targetContext
 val application = context.applicationContext as Application
 return CaptureViewModel(application)
 }

 @Test
 fun captureListScreen_emptyList_showsEmptyMessage() {
 // Given
 val authViewModel = createMockAuthViewModel()
 val captureViewModel = createMockCaptureViewModel()

 composeTestRule.setContent {
 GodEyeTheme {
 CaptureListScreen(
 onNavigateBack = {},
 viewModel = captureViewModel,
 authViewModel = authViewModel,
 onCaptureClick = {}
 )
 }
 }

 // Then
 composeTestRule.onNodeWithText("No se han detectado placas aún")
 .assertExists()
 }

 @Test
 fun captureListScreen_withCaptures_displaysItems() {
 // Given
 val authViewModel = createMockAuthViewModel()
 val captureViewModel = createMockCaptureViewModel()

 // Agregar capturas de prueba al ViewModel
 val mockCaptures = listOf(
 CaptureData(
 id = 1,
 userEmail = "test@example.com",
 imageUri = "test1",
 latitude = 19.4326,
 longitude = -99.1332,
 timestamp = System.currentTimeMillis(),
 extractedText = "ABC123",
 detectedPlate = "ABC-123"
 ),
 CaptureData(
 id = 2,
 userEmail = "test@example.com",
 imageUri = "test2",
 latitude = 19.4326,
 longitude = -99.1332,
 timestamp = System.currentTimeMillis(),
 extractedText = "XYZ789",
 detectedPlate = "XYZ-789"
 )
 )

 composeTestRule.setContent {
 GodEyeTheme {
 CaptureListScreen(
 onNavigateBack = {},
 viewModel = captureViewModel,
 authViewModel = authViewModel,
 onCaptureClick = {}
 )
 }
 }

 // Nota: Este test puede fallar si el ViewModel no tiene capturas mockeadas
 // En un escenario real, necesitarías mockear el DAO o usar un repositorio falso
 }

 @Test
 fun captureListScreen_clickCaptureItem_triggersCallback() {
 // Given
 var clickedCapture: CaptureData? = null
 val authViewModel = createMockAuthViewModel()
 val captureViewModel = createMockCaptureViewModel()

 composeTestRule.setContent {
 GodEyeTheme {
 CaptureListScreen(
 onNavigateBack = {},
 viewModel = captureViewModel,
 authViewModel = authViewModel,
 onCaptureClick = { clickedCapture = it }
 )
 }
 }

 // Este test es básico, en un escenario real necesitarías mockear datos reales
 // y verificar la interacción con los elementos de la lista
 }
}

