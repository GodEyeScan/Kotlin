package com.example.godeye.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Unit Tests para PlateDetector
 * Prueba la lógica de detección de placas mexicanas
 */
class PlateDetectorTest {

 @Test
 fun `detectPlate should detect valid Mexican plate format ABC-123`() {
 // Given
 val text = "La placa es ABC-123"

 // When
 val result = PlateDetector.detectPlate(text)

 // Then
 assertThat(result).isEqualTo("ABC-123")
 }

 @Test
 fun `detectPlate should detect valid Mexican plate format ABC-1234`() {
 // Given
 val text = "Placa detectada: XYZ-9876"

 // When
 val result = PlateDetector.detectPlate(text)

 // Then
 assertThat(result).isEqualTo("XYZ-9876")
 }

 @Test
 fun `detectPlate should detect plate without hyphen ABC123`() {
 // Given
 val text = "ABC123 detectada"

 // When
 val result = PlateDetector.detectPlate(text)

 // Then
 assertThat(result).isNotNull()
 assertThat(result).contains("ABC")
 assertThat(result).contains("123")
 }

 @Test
 fun `detectPlate should return null for invalid format`() {
 // Given
 val text = "No hay placa aquí 12345"

 // When
 val result = PlateDetector.detectPlate(text)

 // Then
 assertThat(result).isNull()
 }

 @Test
 fun `detectPlate should return null for empty string`() {
 // Given
 val text = ""

 // When
 val result = PlateDetector.detectPlate(text)

 // Then
 assertThat(result).isNull()
 }

 @Test
 fun `detectPlate should detect first valid plate in multiple occurrences`() {
 // Given
 val text = "Placas ABC-123 y XYZ-789 detectadas"

 // When
 val result = PlateDetector.detectPlate(text)

 // Then
 assertThat(result).isEqualTo("ABC-123")
 }
}

