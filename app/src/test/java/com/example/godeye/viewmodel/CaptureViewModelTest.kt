package com.example.godeye.viewmodel

import app.cash.turbine.test
import com.example.godeye.data.CaptureData
import com.example.godeye.data.repository.CaptureRepository
import com.example.godeye.domain.usecase.DeleteCaptureUseCase
import com.example.godeye.domain.usecase.GetAllCapturesUseCase
import com.example.godeye.domain.usecase.SaveCaptureUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

/**
 * Unit Tests para CaptureViewModel
 * Prueba la lógica de negocio del ViewModel de forma aislada
 */
@OptIn(ExperimentalCoroutinesApi::class)
/**
 * CaptureViewModelTest
 *
 * Descripción: [Agregar descripción]
 *
 * @author GodEye Team
 * @version 1.0
 * @since 2025-12-07
 */
class CaptureViewModelTest {

 private lateinit var repository: CaptureRepository
 private lateinit var getAllCapturesUseCase: GetAllCapturesUseCase
 private lateinit var saveCaptureUseCase: SaveCaptureUseCase
 private lateinit var deleteCaptureUseCase: DeleteCaptureUseCase

 private val testDispatcher = StandardTestDispatcher()

 @Before
 fun setup() {
 Dispatchers.setMain(testDispatcher)

 // Crear mocks de dependencias
 repository = mockk(relaxed = true)
 getAllCapturesUseCase = GetAllCapturesUseCase(repository)
 saveCaptureUseCase = mockk(relaxed = true)
 deleteCaptureUseCase = mockk(relaxed = true)
 }

 @After
 fun tearDown() {
 Dispatchers.resetMain()
 }

 @Test
 fun `getAllCaptures should return list of captures`() = runTest {
 // Given
 val mockCaptures = listOf(
 CaptureData(
 id = 1,
 userEmail = "test@example.com",
 imageUri = "test_uri_1",
 latitude = 19.4326,
 longitude = -99.1332,
 timestamp = System.currentTimeMillis(),
 extractedText = "ABC123",
 detectedPlate = "ABC-123"
 ),
 CaptureData(
 id = 2,
 userEmail = "test@example.com",
 imageUri = "test_uri_2",
 latitude = 19.4326,
 longitude = -99.1332,
 timestamp = System.currentTimeMillis(),
 extractedText = "XYZ789",
 detectedPlate = "XYZ-789"
 )
 )

 coEvery { repository.getAllCaptures() } returns flowOf(mockCaptures)

 // When & Then
 getAllCapturesUseCase().test {
 val result = awaitItem()
 assertEquals(2, result.size)
 assertEquals("ABC-123", result[0].detectedPlate)
 assertEquals("XYZ-789", result[1].detectedPlate)
 awaitComplete()
 }
 }

 @Test
 fun `saveCapture should insert capture and return success`() = runTest {
 // Given
 val captureData = CaptureData(
 id = 1,
 userEmail = "test@example.com",
 imageUri = "test_uri",
 latitude = 19.4326,
 longitude = -99.1332,
 timestamp = System.currentTimeMillis(),
 extractedText = "ABC123",
 detectedPlate = "ABC-123"
 )

 coEvery { saveCaptureUseCase(captureData) } returns Result.success(1L)

 // When
 val result = saveCaptureUseCase(captureData)

 // Then
 assertTrue(result.isSuccess)
 assertEquals(1L, result.getOrNull())
 coVerify { saveCaptureUseCase(captureData) }
 }

 @Test
 fun `deleteCapture should remove capture successfully`() = runTest {
 // Given
 val captureData = CaptureData(
 id = 1,
 userEmail = "test@example.com",
 imageUri = "test_uri",
 latitude = 19.4326,
 longitude = -99.1332,
 timestamp = System.currentTimeMillis(),
 extractedText = "ABC123",
 detectedPlate = "ABC-123"
 )

 coEvery { deleteCaptureUseCase(captureData) } returns Result.success(Unit)

 // When
 val result = deleteCaptureUseCase(captureData)

 // Then
 assertTrue(result.isSuccess)
 coVerify { deleteCaptureUseCase(captureData) }
 }

 @Test
 fun `saveCapture with empty imageUri should return failure`() = runTest {
 // Given
 val invalidCapture = CaptureData(
 id = 1,
 userEmail = "test@example.com",
 imageUri = "",
 latitude = 0.0,
 longitude = 0.0,
 timestamp = System.currentTimeMillis(),
 extractedText = "",
 detectedPlate = null
 )

 coEvery { saveCaptureUseCase(invalidCapture) } returns Result.failure(
 IllegalArgumentException("URI de imagen no puede estar vacía")
 )

 // When
 val result = saveCaptureUseCase(invalidCapture)

 // Then
 assertTrue(result.isFailure)
 coVerify { saveCaptureUseCase(invalidCapture) }
 }
}

