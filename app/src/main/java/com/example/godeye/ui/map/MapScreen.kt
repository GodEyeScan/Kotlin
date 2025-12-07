/**
 * MapScreen.kt
 *
 * Pantalla con el mapa y el trazado de puntos (tracking) de capturas.
 */

package com.example.godeye.ui.map

import android.content.Context
import android.location.Address
import android.location.Geocoder
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.godeye.data.CaptureData
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
 capture: CaptureData,
 onNavigateBack: () -> Unit,
 captureViewModel: com.example.godeye.viewmodel.CaptureViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
 android.util.Log.d("MapScreen", "Abriendo mapa para captura ID: ${capture.id}, Placa: ${capture.detectedPlate}")

 val context = LocalContext.current
 var locationName by remember { mutableStateOf("Obteniendo ubicación...") }
 val hasValidLocation = capture.latitude != 0.0 && capture.longitude != 0.0
 var allLocations by remember { mutableStateOf<List<CaptureData>>(emptyList()) }

 // Obtener todas las ubicaciones donde se detectó esta placa (TODOS LOS USUARIOS)
 LaunchedEffect(capture.detectedPlate) {
 if (capture.detectedPlate != null && capture.detectedPlate.isNotBlank()) {
 try {
 allLocations = captureViewModel.getAllCapturesByPlateAllUsers(capture.detectedPlate)
 android.util.Log.d("MapScreen", "Ubicaciones cargadas para placa ${capture.detectedPlate}: ${allLocations.size} (todos los usuarios)")
 } catch (e: Exception) {
 android.util.Log.e("MapScreen", "Error al cargar ubicaciones: ${e.message}", e)
 allLocations = emptyList()
 }
 }
 }

 // Obtener nombre de la ubicación
 LaunchedEffect(capture) {
 if (hasValidLocation) {
 locationName = getLocationName(context, capture.latitude, capture.longitude)
 }
 }

 Scaffold(
 topBar = {
 TopAppBar(
 title = { Text("Ubicación de la Placa") },
 navigationIcon = {
 IconButton(onClick = onNavigateBack) {
 Icon(
 Icons.AutoMirrored.Filled.ArrowBack,
 contentDescription = "Volver"
 )
 }
 }
 )
 }
 ) { paddingValues ->
 Column(
 modifier = Modifier
 .fillMaxSize()
 .padding(paddingValues)
 ) {
 if (!hasValidLocation) {
 // Mostrar mensaje de error si no hay ubicación válida
 Box(
 modifier = Modifier
 .fillMaxSize()
 .padding(24.dp),
 contentAlignment = Alignment.Center
 ) {
 Card(
 colors = CardDefaults.cardColors(
 containerColor = MaterialTheme.colorScheme.errorContainer
 )
 ) {
 Column(
 modifier = Modifier.padding(24.dp),
 horizontalAlignment = Alignment.CenterHorizontally
 ) {
 Icon(
 imageVector = Icons.Filled.LocationOff,
 contentDescription = "Sin ubicación",
 modifier = Modifier.size(64.dp),
 tint = MaterialTheme.colorScheme.onErrorContainer
 )
 Spacer(modifier = Modifier.height(16.dp))
 Text(
 text = "No hay ubicación válida",
 style = MaterialTheme.typography.titleLarge,
 color = MaterialTheme.colorScheme.onErrorContainer
 )
 Spacer(modifier = Modifier.height(8.dp))
 Text(
 text = "Esta captura no tiene coordenadas GPS registradas.",
 style = MaterialTheme.typography.bodyMedium,
 color = MaterialTheme.colorScheme.onErrorContainer
 )
 }
 }
 }
 } else {
 // Card con información de ubicación
 Card(
 modifier = Modifier
 .fillMaxWidth()
 .padding(16.dp),
 colors = CardDefaults.cardColors(
 containerColor = MaterialTheme.colorScheme.primaryContainer
 )
 ) {
 Column(
 modifier = Modifier.padding(16.dp),
 horizontalAlignment = Alignment.CenterHorizontally
 ) {
 Text(
 text = "Placa ubicada en:",
 style = MaterialTheme.typography.titleMedium,
 color = MaterialTheme.colorScheme.onPrimaryContainer
 )
 Spacer(modifier = Modifier.height(8.dp))
 Text(
 text = locationName,
 style = MaterialTheme.typography.bodyLarge,
 color = MaterialTheme.colorScheme.onPrimaryContainer
 )
 capture.detectedPlate?.let { plate ->
 Spacer(modifier = Modifier.height(8.dp))
 Text(
 text = "Placa: $plate",
 style = MaterialTheme.typography.titleLarge,
 color = MaterialTheme.colorScheme.primary
 )
 }
 }
 }

 // Mapa
 val position = LatLng(capture.latitude, capture.longitude)
 val cameraPositionState = rememberCameraPositionState {
 this.position = CameraPosition.fromLatLngZoom(position, 13f)
 }

 GoogleMap(
 modifier = Modifier
 .fillMaxWidth()
 .weight(1f),
 cameraPositionState = cameraPositionState,
 properties = MapProperties(
 isMyLocationEnabled = false
 ),
 uiSettings = MapUiSettings(
 zoomControlsEnabled = true,
 myLocationButtonEnabled = false
 )
 ) {
 // Marcador ROJO para la ubicación actual (la que se hizo clic)
 Marker(
 state = MarkerState(position = position),
 title = "Ubicacion Actual",
 snippet = "Placa: ${capture.detectedPlate ?: "No detectada"}\nFecha: ${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(capture.timestamp))}",
 icon = com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker(
 com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_RED
 )
 )

 // Marcadores AZULES para otras ubicaciones donde se vio la misma placa
 allLocations.filter { it.id != capture.id && it.latitude != 0.0 && it.longitude != 0.0 }.forEach { location ->
 val locationPosition = LatLng(location.latitude, location.longitude)
 val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
 val formattedDate = dateFormat.format(java.util.Date(location.timestamp))

 Marker(
 state = MarkerState(position = locationPosition),
 title = "Vista anterior",
 snippet = "Placa: ${location.detectedPlate}\nFecha: $formattedDate\nUsuario: ${location.userEmail.substringBefore("@")}",
 icon = com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker(
 com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_AZURE
 )
 )
 }
 }

 // Información adicional
 if (allLocations.size > 1) {
 Card(
 modifier = Modifier
 .fillMaxWidth()
 .padding(horizontal = 16.dp, vertical = 8.dp),
 colors = CardDefaults.cardColors(
 containerColor = MaterialTheme.colorScheme.secondaryContainer
 )
 ) {
 Row(
 modifier = Modifier.padding(12.dp),
 verticalAlignment = Alignment.CenterVertically
 ) {
 Icon(
 imageVector = Icons.Filled.LocationOff,
 contentDescription = null,
 tint = MaterialTheme.colorScheme.onSecondaryContainer
 )
 Spacer(modifier = Modifier.width(12.dp))
 Column {
 Text(
 text = "Historial de tracking",
 style = MaterialTheme.typography.titleSmall,
 color = MaterialTheme.colorScheme.onSecondaryContainer
 )
 Text(
 text = "Total de avistamientos: ${allLocations.size}",
 style = MaterialTheme.typography.bodyMedium,
 color = MaterialTheme.colorScheme.onSecondaryContainer
 )
 Text(
 text = "Rojo = Actual | Azul = Anteriores",
 style = MaterialTheme.typography.bodySmall,
 color = MaterialTheme.colorScheme.onSecondaryContainer
 )
 Text(
 text = "Toca los marcadores para ver detalles",
 style = MaterialTheme.typography.bodySmall,
 color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
 )
 }
 }
 }
 }

 // Botón de regresar
 Button(
 onClick = onNavigateBack,
 modifier = Modifier
 .fillMaxWidth()
 .padding(16.dp)
 ) {
 Text("Regresar al Historial")
 }
 }
 }
 }
}

private suspend fun getLocationName(context: Context, latitude: Double, longitude: Double): String {
 return withContext(Dispatchers.IO) {
 try {
 val geocoder = Geocoder(context, Locale.getDefault())
 val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)

 if (!addresses.isNullOrEmpty()) {
 val address = addresses[0]
 buildString {
 // Intentar obtener el nombre de la calle o localidad
 address.thoroughfare?.let { append("$it, ") }
 address.subLocality?.let { append("$it, ") }
 address.locality?.let { append("$it, ") }
 address.adminArea?.let { append(it) }

 if (isEmpty()) {
 append("${String.format("%.6f", latitude)}, ${String.format("%.6f", longitude)}")
 }
 }
 } else {
 "${String.format("%.6f", latitude)}, ${String.format("%.6f", longitude)}"
 }
 } catch (e: Exception) {
 "${String.format("%.6f", latitude)}, ${String.format("%.6f", longitude)}"
 }
 }
}
