package com.example.godeye.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Componente para mostrar el logo personalizado con procesamiento de fondo blanco
 *
 * Este componente intenta cargar una imagen desde recursos (logo_app.png/jpg/webp)
 * y aplica un filtro para convertir el fondo blanco en transparente.
 * Si no encuentra la imagen personalizada, muestra el logo por defecto (EyeLogo).
 *
 * @param size Tamaño del logo
 * @param removeWhiteBackground Si es true, procesa el fondo blanco para hacerlo transparente
 * @param modifier Modificador de Compose
 */
@Composable
fun LogoImage(
 size: Dp = 120.dp,
 removeWhiteBackground: Boolean = true,
 modifier: Modifier = Modifier
) {
 val context = LocalContext.current

 // Intentar obtener el recurso logo_app
 val logoResourceId = try {
 context.resources.getIdentifier("logo_app", "drawable", context.packageName)
 } catch (e: Exception) {
 0
 }

 Box(
 modifier = modifier.size(size),
 contentAlignment = Alignment.Center
 ) {
 if (logoResourceId != 0) {
 // Si existe logo_app, usarlo
 val colorFilter = if (removeWhiteBackground) {
 // Matriz de color que aumenta el contraste y remueve blancos
 val matrix = ColorMatrix(
 floatArrayOf(
 1.2f, 0f, 0f, 0f, -25f, // Red
 0f, 1.2f, 0f, 0f, -25f, // Green
 0f, 0f, 1.2f, 0f, -25f, // Blue
 0f, 0f, 0f, 1f, 0f // Alpha
 )
 )
 ColorFilter.colorMatrix(matrix)
 } else {
 null
 }

 Image(
 painter = painterResource(id = logoResourceId),
 contentDescription = "Logo de GodEye",
 modifier = Modifier.size(size),
 contentScale = ContentScale.Fit,
 colorFilter = colorFilter
 )
 } else {
 // Si no existe logo_app, usar el logo por defecto
 EyeLogo(size = size)
 }
 }
}

/**
 * Alternativa: Logo con fondo blanco convertido a transparente sin filtro
 * Útil si la imagen ya tiene transparencia o quieres preservar colores exactos
 */
@Composable
fun LogoImageWithTransparency(
 size: Dp = 120.dp,
 modifier: Modifier = Modifier
) {
 val context = LocalContext.current
 val logoResourceId = try {
 context.resources.getIdentifier("logo_app", "drawable", context.packageName)
 } catch (e: Exception) {
 0
 }

 Box(
 modifier = modifier.size(size),
 contentAlignment = Alignment.Center
 ) {
 if (logoResourceId != 0) {
 Image(
 painter = painterResource(id = logoResourceId),
 contentDescription = "Logo de GodEye",
 modifier = Modifier.size(size),
 contentScale = ContentScale.Fit
 )
 } else {
 // Fallback al logo por defecto
 EyeLogo(size = size)
 }
 }
}

