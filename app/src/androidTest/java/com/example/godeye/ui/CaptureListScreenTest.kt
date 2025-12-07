package com.example.godeye.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.godeye.data.CaptureData
import com.example.godeye.ui.captures.CaptureListScreen
import com.example.godeye.ui.theme.GodEyeTheme
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

 @Test
 fun captureListScreen_emptyList_showsEmptyMessage() {
 // Given
 composeTestRule.setContent {
 GodEyeTheme {
 CaptureListScreen(
 captures = emptyList(),
 onViewMap = {},
 onDeleteCapture = {}
 )
 }
 }

 // Then
 composeTestRule.onNodeWithText("No hay capturas registradas")
 .assertExists()
 }

 @Test
 fun captureListScreen_withCaptures_displaysItems() {
 // Given
 val mockCaptures = listOf(
 CaptureData(
 imageUri = "test1",
 latitude = 19.4326,
 longitude = -99.1332,
 timestamp = System.currentTimeMillis(),
 extractedText = "ABC123",
 detectedPlate = "ABC-123"
 ),
 CaptureData(
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
 captures = mockCaptures,
 onViewMap = {},
 onDeleteCapture = {}
 )
 }
 }

 // Then
 composeTestRule.onNodeWithText("ABC-123").assertExists()
 composeTestRule.onNodeWithText("XYZ-789").assertExists()
 }

 @Test
 fun captureListScreen_clickDeleteButton_triggersCallback() {
 // Given
 var deletedCapture: CaptureData? = null
 val capture = CaptureData(
 imageUri = "test",
 latitude = 19.4326,
 longitude = -99.1332,
 timestamp = System.currentTimeMillis(),
 extractedText = "ABC123",
 detectedPlate = "ABC-123"
 )

 composeTestRule.setContent {
 GodEyeTheme {
 CaptureListScreen(
 captures = listOf(capture),
 onViewMap = {},
 onDeleteCapture = { deletedCapture = it }
 )
 }
 }

 // When
 composeTestRule.onNodeWithContentDescription("Eliminar captura").performClick()

 // Then
 assert(deletedCapture == capture)
 }
}

