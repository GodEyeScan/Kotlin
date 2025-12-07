package com.example.godeye.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.godeye.ui.theme.*

@Composable
fun EyeLogo(
 size: Dp = 120.dp,
 modifier: Modifier = Modifier
) {
 Canvas(modifier = modifier.size(size)) {
 val canvasWidth = size.toPx()
 val canvasHeight = size.toPx()
 val centerX = canvasWidth / 2
 val centerY = canvasHeight / 2

 // Dibujar el contorno del ojo (elipse)
 val eyePath = Path().apply {
 moveTo(centerX - canvasWidth * 0.4f, centerY)
 cubicTo(
 centerX - canvasWidth * 0.4f, centerY - canvasHeight * 0.25f,
 centerX + canvasWidth * 0.4f, centerY - canvasHeight * 0.25f,
 centerX + canvasWidth * 0.4f, centerY
 )
 cubicTo(
 centerX + canvasWidth * 0.4f, centerY + canvasHeight * 0.25f,
 centerX - canvasWidth * 0.4f, centerY + canvasHeight * 0.25f,
 centerX - canvasWidth * 0.4f, centerY
 )
 close()
 }

 // Gradiente para el contorno del ojo
 val eyeGradient = Brush.linearGradient(
 colors = listOf(Primary, PrimaryLight),
 start = Offset(0f, 0f),
 end = Offset(canvasWidth, canvasHeight)
 )

 // Dibujar el fondo del ojo
 drawPath(
 path = eyePath,
 brush = eyeGradient,
 style = Fill
 )

 // Dibujar el borde del ojo
 drawPath(
 path = eyePath,
 color = PrimaryVariant,
 style = Stroke(width = 4f)
 )

 // Dibujar el iris (círculo grande)
 val irisRadius = canvasWidth * 0.18f
 val irisGradient = Brush.radialGradient(
 colors = listOf(Accent, AccentLight, Primary),
 center = Offset(centerX, centerY),
 radius = irisRadius
 )

 drawCircle(
 brush = irisGradient,
 radius = irisRadius,
 center = Offset(centerX, centerY)
 )

 // Dibujar el borde del iris
 drawCircle(
 color = Secondary,
 radius = irisRadius,
 center = Offset(centerX, centerY),
 style = Stroke(width = 3f)
 )

 // Dibujar la pupila (círculo negro)
 val pupilRadius = canvasWidth * 0.08f
 drawCircle(
 color = Color(0xFF1A1F2E),
 radius = pupilRadius,
 center = Offset(centerX, centerY)
 )

 // Dibujar el brillo en la pupila (pequeño círculo blanco)
 drawCircle(
 color = Color.White,
 radius = canvasWidth * 0.03f,
 center = Offset(centerX - pupilRadius * 0.3f, centerY - pupilRadius * 0.3f)
 )

 // Líneas decorativas alrededor (rayos)
 val numRays = 8
 val rayLength = canvasWidth * 0.1f
 val rayStartRadius = canvasWidth * 0.35f

 for (i in 0 until numRays) {
 val angle = (i * 360f / numRays) * Math.PI / 180f
 val startX = centerX + rayStartRadius * kotlin.math.cos(angle).toFloat()
 val startY = centerY + rayStartRadius * kotlin.math.sin(angle).toFloat()
 val endX = centerX + (rayStartRadius + rayLength) * kotlin.math.cos(angle).toFloat()
 val endY = centerY + (rayStartRadius + rayLength) * kotlin.math.sin(angle).toFloat()

 drawLine(
 brush = Brush.linearGradient(
 colors = listOf(Primary.copy(alpha = 0.6f), Color.Transparent),
 start = Offset(startX, startY),
 end = Offset(endX, endY)
 ),
 start = Offset(startX, startY),
 end = Offset(endX, endY),
 strokeWidth = 3f
 )
 }
 }
}

