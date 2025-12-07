/**
 * ReportScreen.kt
 *
 * Pantalla de detalle/creación de reportes.
 */

package com.example.godeye.ui.report

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.godeye.data.CaptureData
import com.example.godeye.viewmodel.AuthViewModel
import com.example.godeye.viewmodel.CaptureViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ReportScreen(
 authViewModel: AuthViewModel,
 captureViewModel: CaptureViewModel = viewModel(),
 onNavigateToAdminReports: () -> Unit = {}
) {
 val currentUser = authViewModel.currentUser.value
 val userProfile = authViewModel.userProfile.value // Perfil local con nombre, teléfono, NIT
 val isAdmin = authViewModel.isAdmin.value
 val scrollState = rememberScrollState()
 val focusRequester = remember { FocusRequester() }
 val context = LocalContext.current
 val locationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
 val coroutineScope = rememberCoroutineScope()

 var plateNumber by remember { mutableStateOf("") }
 var reportReason by remember { mutableStateOf("") }
 var showSuccessDialog by remember { mutableStateOf(false) }
 var isSubmitting by remember { mutableStateOf(false) }
 var errorMessage by remember { mutableStateOf<String?>(null) }

 // Si es admin, redirigir automáticamente al panel de administrador
 LaunchedEffect(isAdmin) {
 if (isAdmin) {
 onNavigateToAdminReports()
 }
 }

 // Si no es admin, mostrar formulario de reporte
 if (!isAdmin) {
 // Auto-scroll y focus al campo de placa
 LaunchedEffect(Unit) {
 delay(300) // Pequeño delay para asegurar que el layout esté listo
 focusRequester.requestFocus()
 }

 Scaffold(
 topBar = {
 TopAppBar(
 title = { Text("Reportar Placa") }
 )
 }
 ) { paddingValues ->
 Column(
 modifier = Modifier
 .fillMaxSize()
 .padding(paddingValues)
 .padding(24.dp)
 .verticalScroll(scrollState)
 ) {
 Text(
 text = "Información del Reportante",
 style = MaterialTheme.typography.titleLarge,
 color = MaterialTheme.colorScheme.primary
 )

 Spacer(modifier = Modifier.height(16.dp))

 currentUser?.let { user ->
 // Campos prellenados de información del usuario (usando perfil local)
 OutlinedTextField(
 value = userProfile?.name ?: user.name,
 onValueChange = {},
 label = { Text("Nombre") },
 leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
 readOnly = true,
 enabled = false,
 modifier = Modifier.fillMaxWidth()
 )

 Spacer(modifier = Modifier.height(12.dp))

 OutlinedTextField(
 value = user.email,
 onValueChange = {},
 label = { Text("Email") },
 leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
 readOnly = true,
 enabled = false,
 modifier = Modifier.fillMaxWidth()
 )

 Spacer(modifier = Modifier.height(12.dp))

 OutlinedTextField(
 value = userProfile?.phone ?: "${user.phonePrefix} ${user.phoneNumber}",
 onValueChange = {},
 label = { Text("Teléfono") },
 leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null) },
 readOnly = true,
 enabled = false,
 modifier = Modifier.fillMaxWidth()
 )

 Spacer(modifier = Modifier.height(12.dp))

 OutlinedTextField(
 value = userProfile?.nit ?: user.nit,
 onValueChange = {},
 label = { Text("NIT") },
 leadingIcon = { Icon(Icons.Filled.AccountCircle, contentDescription = null) },
 readOnly = true,
 enabled = false,
 modifier = Modifier.fillMaxWidth()
 )

 Spacer(modifier = Modifier.height(32.dp))

 // Sección de reporte
 Text(
 text = "Información del Reporte",
 style = MaterialTheme.typography.titleLarge,
 color = MaterialTheme.colorScheme.primary
 )

 Spacer(modifier = Modifier.height(16.dp))

 // Campo de placa (con autofocus)
 OutlinedTextField(
 value = plateNumber,
 onValueChange = { plateNumber = it.uppercase() },
 label = { Text("Placa a Reportar *") },
 placeholder = { Text("Ej: ABC123") },
 supportingText = { Text("Ingrese la placa sin espacios") },
 keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
 singleLine = true,
 modifier = Modifier
 .fillMaxWidth()
 .focusRequester(focusRequester)
 )

 Spacer(modifier = Modifier.height(16.dp))

 // Campo de razón del reporte
 OutlinedTextField(
 value = reportReason,
 onValueChange = { reportReason = it },
 label = { Text("Razón del Reporte *") },
 leadingIcon = { Icon(Icons.Filled.Description, contentDescription = null) },
 placeholder = { Text("Describa el motivo del reporte") },
 supportingText = { Text("Mínimo 10 caracteres") },
 minLines = 4,
 maxLines = 6,
 modifier = Modifier.fillMaxWidth()
 )

 Spacer(modifier = Modifier.height(32.dp))

 // Botón de enviar reporte
 Button(
 onClick = {
 if (plateNumber.isNotBlank() && reportReason.length >= 10 && !isSubmitting) {
 isSubmitting = true
 errorMessage = null

 // Obtener ubicación y guardar reporte
 coroutineScope.launch {
 val location = getCurrentLocation(context, locationPermission.status.isGranted)

 val captureData = CaptureData(
 id = System.currentTimeMillis(),
 userEmail = currentUser.email, // Identificar al propietario
 imageUri = "", // No hay imagen en reportes manuales
 latitude = location?.latitude ?: 0.0,
 longitude = location?.longitude ?: 0.0,
 timestamp = System.currentTimeMillis(),
 extractedText = reportReason,
 detectedPlate = plateNumber
 )

 android.util.Log.d("ReportScreen", "Creando reporte manual para placa: $plateNumber")
 android.util.Log.d("ReportScreen", "Enviando POST a la API...")

 captureViewModel.addCapture(
 captureData = captureData,
 token = authViewModel.accessToken.value,
 userEmail = currentUser.email
 ) { success, error ->
 isSubmitting = false
 if (success) {
 android.util.Log.i("ReportScreen", "Reporte creado exitosamente para placa: $plateNumber")
 android.util.Log.i("ReportScreen", "- Guardado local: ✅")
 android.util.Log.i("ReportScreen", "- POST a API: ${if (error?.contains("sincronizado") == true) "✅" else "⚠ (solo local)"}")
 showSuccessDialog = true
 } else {
 android.util.Log.e("ReportScreen", "Error al crear reporte: $error")
 errorMessage = error ?: "Error al guardar el reporte"
 }
 }
 }
 }
 },
 modifier = Modifier
 .fillMaxWidth()
 .height(50.dp),
 enabled = plateNumber.isNotBlank() && reportReason.length >= 10 && !isSubmitting
 ) {
 if (isSubmitting) {
 CircularProgressIndicator(
 modifier = Modifier.size(20.dp),
 color = MaterialTheme.colorScheme.onPrimary
 )
 } else {
 Icon(
 imageVector = Icons.AutoMirrored.Filled.Send,
 contentDescription = null,
 modifier = Modifier.size(20.dp)
 )
 }
 Spacer(modifier = Modifier.width(8.dp))
 Text(if (isSubmitting) "Enviando..." else "Enviar Reporte")
 }

 // Mostrar error si existe
 if (errorMessage != null) {
 Spacer(modifier = Modifier.height(8.dp))
 Text(
 text = errorMessage ?: "",
 color = MaterialTheme.colorScheme.error,
 style = MaterialTheme.typography.bodySmall
 )
 }

 Spacer(modifier = Modifier.height(16.dp))
 }
 }
 }

 // Diálogo de confirmación
 if (showSuccessDialog) {
 AlertDialog(
 onDismissRequest = {
 showSuccessDialog = false
 plateNumber = ""
 reportReason = ""
 },
 icon = { Icon(Icons.Filled.CheckCircle, contentDescription = null) },
 title = { Text("Reporte Enviado") },
 text = {
 Text("El reporte de la placa $plateNumber ha sido registrado exitosamente y está disponible en el panel de administrador.")
 },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSuccessDialog = false
                        plateNumber = ""
                        reportReason = ""
                    }
                ) {
                    Text("Aceptar")
                }
            }
        )
    }
    }
}

/**
 * Función suspendida para obtener la ubicación GPS actual
 */
@SuppressLint("MissingPermission")
suspend fun getCurrentLocation(context: Context, hasPermission: Boolean): Location? {
    if (!hasPermission) return null

    return try {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        fusedLocationClient.lastLocation.await()
    } catch (_: Exception) {
        null
    }
}
