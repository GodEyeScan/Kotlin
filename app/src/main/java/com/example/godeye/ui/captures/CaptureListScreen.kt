/**
 * CaptureListScreen.kt
 *
 * Pantalla que muestra la lista de capturas (historial) del usuario.
 */

package com.example.godeye.ui.captures

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.godeye.data.CaptureData
import com.example.godeye.viewmodel.CaptureViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaptureListScreen(
 onNavigateBack: () -> Unit,
 viewModel: CaptureViewModel = viewModel(),
 authViewModel: com.example.godeye.viewmodel.AuthViewModel,
 onCaptureClick: (CaptureData) -> Unit
) {
 val captures = viewModel.captures
 val currentUser = authViewModel.currentUser.value
 val isDeveloper = currentUser?.userType == com.example.godeye.data.UserType.DEVELOPER
 val isAdmin = authViewModel.isAdmin.value
 val token = authViewModel.accessToken.value

 // Forzar recarga de datos del usuario actual al abrir la pantalla
 androidx.compose.runtime.LaunchedEffect(currentUser?.email) {
 currentUser?.email?.let { email ->
 android.util.Log.d("CaptureListScreen", "Recargando datos para usuario: $email")
 android.util.Log.d("CaptureListScreen", "Captures actuales en lista: ${captures.size}")

 // Debug: Ver cuántas capturas hay por usuario en BD
 val capturesByUser = viewModel.debugCaptureCount()
 android.util.Log.d("CaptureListScreen", "=== DEBUG: Capturas en BD por usuario ===")
 capturesByUser.forEach { (user, count) ->
 android.util.Log.d("CaptureListScreen", "Usuario: $user -> $count capturas")
 }
 android.util.Log.d("CaptureListScreen", "========================================")

 viewModel.setCurrentUser(email)
 }
 }

 // Cargar reportes al entrar a la pantalla
 androidx.compose.runtime.LaunchedEffect(isAdmin, token) {
 if (isAdmin && token != null) {
 viewModel.loadAllReportsFromApi(token)
 } else if (token != null) {
 viewModel.loadUserReportsFromApi(token)
 }
 }

 Scaffold(
 topBar = {
 TopAppBar(
 title = {
 Text(if (isAdmin) "Todos los Reportes (Admin)" else "Placas Detectadas")
 },
 navigationIcon = {
 IconButton(onClick = onNavigateBack) {
 Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
 }
 }
 )
 }
 ) { paddingValues ->
 if (viewModel.isLoading.value) {
 Box(
 modifier = Modifier
 .fillMaxSize()
 .padding(paddingValues),
 contentAlignment = Alignment.Center
 ) {
 CircularProgressIndicator()
 }
 } else if (viewModel.captures.isEmpty()) {
 Box(
 modifier = Modifier
 .fillMaxSize()
 .padding(paddingValues),
 contentAlignment = Alignment.Center
 ) {
 Text(
 text = if (isAdmin) {
 "No hay reportes en el sistema"
 } else {
 "No se han detectado placas aún\n\nInicia la captura automática o toma fotos manualmente"
 },
 style = MaterialTheme.typography.bodyLarge,
 textAlign = androidx.compose.ui.text.style.TextAlign.Center
 )
 }
 } else {
 LazyColumn(
 modifier = Modifier
 .fillMaxSize()
 .padding(paddingValues)
 .padding(16.dp),
 verticalArrangement = Arrangement.spacedBy(16.dp)
 ) {
 items(captures) { capture ->
 CaptureCard(
 capture = capture,
 showOcrText = isDeveloper,
 onClick = { onCaptureClick(capture) }
 )
 }
 }
 }

 // Mostrar error si existe
 viewModel.errorMessage.value?.let { error ->
 androidx.compose.runtime.LaunchedEffect(error) {
 // Aquí podrías mostrar un Snackbar o Toast
 }
 }
}

@Composable
fun CaptureCard(
 capture: CaptureData,
 showOcrText: Boolean = false,
 onClick: () -> Unit = {}
) {
 Card(
 modifier = Modifier
 .fillMaxWidth()
 .clickable(onClick = onClick),
 elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
 ) {
 Column(
 modifier = Modifier.padding(16.dp)
 ) {
 // Imagen
 Image(
 painter = rememberAsyncImagePainter(Uri.parse(capture.imageUri)),
 contentDescription = "Captura",
 modifier = Modifier
 .fillMaxWidth()
 .height(200.dp),
 contentScale = ContentScale.Crop
 )

 Spacer(modifier = Modifier.height(12.dp))

 // Placa detectada (destacada)
 capture.detectedPlate?.let { plate ->
 Card(
 modifier = Modifier.fillMaxWidth(),
 colors = CardDefaults.cardColors(
 containerColor = if (capture.isReported) {
 MaterialTheme.colorScheme.errorContainer
 } else {
 MaterialTheme.colorScheme.primaryContainer
 }
 )
 ) {
 Column(
 modifier = Modifier.padding(12.dp),
 horizontalAlignment = Alignment.CenterHorizontally
 ) {
 Text(
 text = if (capture.isReported) "⚠️ PLACA REPORTADA" else "PLACA DETECTADA",
 style = MaterialTheme.typography.labelMedium,
 color = if (capture.isReported) {
 MaterialTheme.colorScheme.onErrorContainer
 } else {
 MaterialTheme.colorScheme.onPrimaryContainer
 }
 )
 Text(
 text = plate,
 style = MaterialTheme.typography.headlineMedium,
 color = if (capture.isReported) {
 MaterialTheme.colorScheme.error
 } else {
 MaterialTheme.colorScheme.primary
 }
 )
 }
 }

 Spacer(modifier = Modifier.height(12.dp))
 }

 // Texto extraído (solo para desarrolladores)
 if (showOcrText && capture.extractedText.isNotEmpty()) {
 Text(
 text = "Texto Extraído (OCR) - Solo Dev:",
 style = MaterialTheme.typography.titleSmall,
 color = MaterialTheme.colorScheme.error
 )
 Card(
 modifier = Modifier.fillMaxWidth(),
 colors = CardDefaults.cardColors(
 containerColor = MaterialTheme.colorScheme.errorContainer
 )
 ) {
 Text(
 text = capture.extractedText.take(150) +
 if (capture.extractedText.length > 150) "..." else "",
 style = MaterialTheme.typography.bodySmall,
 color = MaterialTheme.colorScheme.onErrorContainer,
 modifier = Modifier.padding(8.dp)
 )
 }

 Spacer(modifier = Modifier.height(8.dp))
 }

 // Información de ubicación
 Text(
 text = "Información de Ubicación",
 style = MaterialTheme.typography.titleMedium
 )

 Spacer(modifier = Modifier.height(8.dp))

 Row(
 modifier = Modifier.fillMaxWidth(),
 horizontalArrangement = Arrangement.SpaceBetween
 ) {
 Column(modifier = Modifier.weight(1f)) {
 Text(
 text = "Latitud:",
 style = MaterialTheme.typography.labelMedium
 )
 Text(
 text = String.format("%.6f", capture.latitude),
 style = MaterialTheme.typography.bodyMedium
 )
 }

 Column(modifier = Modifier.weight(1f)) {
 Text(
 text = "Longitud:",
 style = MaterialTheme.typography.labelMedium
 )
 Text(
 text = String.format("%.6f", capture.longitude),
 style = MaterialTheme.typography.bodyMedium
 )
 }
 }

 Spacer(modifier = Modifier.height(8.dp))

 // Fecha y hora
 Text(
 text = "Fecha:",
 style = MaterialTheme.typography.labelMedium
 )
 Text(
 text = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
 .format(Date(capture.timestamp)),
 style = MaterialTheme.typography.bodyMedium
 )

 Spacer(modifier = Modifier.height(8.dp))

 // URI de la imagen
 Text(
 text = "Archivo:",
 style = MaterialTheme.typography.labelMedium
 )
 Text(
 text = capture.imageUri.substringAfterLast("/"),
 style = MaterialTheme.typography.bodySmall
 )
 }
 }
}
