package com.example.godeye.ui.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.godeye.data.CaptureData
import com.example.godeye.viewmodel.AuthViewModel
import com.example.godeye.viewmodel.CaptureViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminReportsScreen(
 authViewModel: AuthViewModel,
 captureViewModel: CaptureViewModel = viewModel(),
 onBack: () -> Unit = {}
) {
 val token = authViewModel.accessToken.value
 var allReports by remember { mutableStateOf<List<CaptureData>>(emptyList()) }
 var isLoading by remember { mutableStateOf(true) }
 var errorMessage by remember { mutableStateOf<String?>(null) }
 var selectedReport by remember { mutableStateOf<CaptureData?>(null) }

 // Cargar todos los reportes al iniciar
 LaunchedEffect(Unit) {
 isLoading = true
 errorMessage = null

 // Intentar obtener reportes desde la API primero (todos los reportes del sistema)
 if (token != null) {
 captureViewModel.getAllReportsFromApi(token) { success, apiReports, error ->
 if (success && apiReports != null) {
 allReports = apiReports
 isLoading = false
 if (apiReports.isEmpty()) {
 errorMessage = "No hay reportes disponibles en el sistema"
 }
 } else {
 // Si falla la API, usar base de datos local como respaldo
 errorMessage = "Usando reportes locales: ${error ?: "Sin conexión"}"
 captureViewModel.getAllCapturesFromLocal { localReports ->
 allReports = localReports
 isLoading = false
 if (localReports.isEmpty()) {
 errorMessage = "No hay reportes disponibles"
 }
 }
 }
 }
 } else {
 // Sin token, usar base de datos local
 captureViewModel.getAllCapturesFromLocal { localReports ->
 allReports = localReports
 isLoading = false
 if (localReports.isEmpty()) {
 errorMessage = "No hay reportes disponibles"
 }
 }
 }
 }

 Scaffold(
 topBar = {
 TopAppBar(
 title = {
 Column {
 Text("Panel de Administrador")
 Text(
 text = "Todos los Reportes",
 style = MaterialTheme.typography.bodySmall
 )
 }
 },
 actions = {
 IconButton(
 onClick = {
 // Recargar reportes desde la API
 isLoading = true
 errorMessage = null
 if (token != null) {
 captureViewModel.getAllReportsFromApi(token) { success, apiReports, error ->
 if (success && apiReports != null) {
 allReports = apiReports
 isLoading = false
 if (apiReports.isEmpty()) {
 errorMessage = "No hay reportes disponibles en el sistema"
 }
 } else {
 errorMessage = error ?: "Error al recargar"
 isLoading = false
 }
 }
 }
 },
 enabled = !isLoading
 ) {
 Icon(
 imageVector = Icons.Filled.Refresh,
 contentDescription = "Refrescar reportes"
 )
 }
 },
 colors = TopAppBarDefaults.topAppBarColors(
 containerColor = MaterialTheme.colorScheme.primaryContainer
 )
 )
 }
 ) { paddingValues ->
 Column(
 modifier = Modifier
 .fillMaxSize()
 .padding(paddingValues)
 ) {
 // Estadísticas generales
 Card(
 modifier = Modifier
 .fillMaxWidth()
 .padding(16.dp),
 colors = CardDefaults.cardColors(
 containerColor = MaterialTheme.colorScheme.secondaryContainer
 )
 ) {
 Row(
 modifier = Modifier
 .fillMaxWidth()
 .padding(16.dp),
 horizontalArrangement = Arrangement.SpaceEvenly
 ) {
 StatCard(
 icon = Icons.Filled.Receipt,
 label = "Total Reportes",
 value = allReports.size.toString()
 )

 StatCard(
 icon = Icons.Filled.CheckCircle,
 label = "Con Placa",
 value = allReports.count { !it.detectedPlate.isNullOrEmpty() }.toString()
 )

 StatCard(
 icon = Icons.Filled.Place,
 label = "Con GPS",
 value = allReports.count { it.latitude != 0.0 && it.longitude != 0.0 }.toString()
 )
 }
 }

 // Lista de reportes
 when {
 isLoading -> {
 Box(
 modifier = Modifier.fillMaxSize(),
 contentAlignment = Alignment.Center
 ) {
 Column(
 horizontalAlignment = Alignment.CenterHorizontally
 ) {
 CircularProgressIndicator()
 Spacer(modifier = Modifier.height(16.dp))
 Text("Cargando reportes...")
 }
 }
 }

 allReports.isEmpty() -> {
 Box(
 modifier = Modifier.fillMaxSize(),
 contentAlignment = Alignment.Center
 ) {
 Column(
 horizontalAlignment = Alignment.CenterHorizontally,
 modifier = Modifier.padding(32.dp)
 ) {
 Icon(
 imageVector = Icons.Filled.Inbox,
 contentDescription = null,
 modifier = Modifier.size(64.dp),
 tint = MaterialTheme.colorScheme.onSurfaceVariant
 )
 Spacer(modifier = Modifier.height(16.dp))
 Text(
 text = "No hay reportes disponibles",
 style = MaterialTheme.typography.titleMedium
 )
 if (errorMessage != null) {
 Spacer(modifier = Modifier.height(8.dp))
 Text(
 text = errorMessage ?: "",
 style = MaterialTheme.typography.bodyMedium,
 color = MaterialTheme.colorScheme.onSurfaceVariant
 )
 }
 }
 }
 }

 else -> {
 LazyColumn(
 modifier = Modifier.fillMaxSize(),
 contentPadding = PaddingValues(16.dp),
 verticalArrangement = Arrangement.spacedBy(12.dp)
 ) {
 items(allReports.sortedByDescending { it.timestamp }) { report ->
 AdminReportCard(
 report = report,
 onClick = { selectedReport = report }
 )
 }
 }
 }
 }
 }
 }

 // Diálogo de detalles del reporte
 if (selectedReport != null) {
 ReportDetailsDialog(
 report = selectedReport!!,
 onDismiss = { selectedReport = null }
 )
 }
}

@Composable
fun StatCard(
 icon: androidx.compose.ui.graphics.vector.ImageVector,
 label: String,
 value: String
) {
 Column(
 horizontalAlignment = Alignment.CenterHorizontally
 ) {
 Icon(
 imageVector = icon,
 contentDescription = null,
 modifier = Modifier.size(32.dp),
 tint = MaterialTheme.colorScheme.onSecondaryContainer
 )
 Spacer(modifier = Modifier.height(4.dp))
 Text(
 text = value,
 style = MaterialTheme.typography.headlineMedium,
 fontWeight = FontWeight.Bold,
 color = MaterialTheme.colorScheme.onSecondaryContainer
 )
 Text(
 text = label,
 style = MaterialTheme.typography.bodySmall,
 color = MaterialTheme.colorScheme.onSecondaryContainer
 )
 }
}

@Composable
fun AdminReportCard(
 report: CaptureData,
 onClick: () -> Unit = {}
) {
 val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

 Card(
 modifier = Modifier
 .fillMaxWidth()
 .clickable { onClick() },
 elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
 ) {
 Column(
 modifier = Modifier
 .fillMaxWidth()
 .padding(16.dp)
 ) {
 // Header con placa
 Row(
 modifier = Modifier.fillMaxWidth(),
 horizontalArrangement = Arrangement.SpaceBetween,
 verticalAlignment = Alignment.CenterVertically
 ) {
 Row(
 verticalAlignment = Alignment.CenterVertically
 ) {
 Icon(
 imageVector = Icons.Filled.DirectionsCar,
 contentDescription = null,
 tint = MaterialTheme.colorScheme.primary,
 modifier = Modifier.size(24.dp)
 )
 Spacer(modifier = Modifier.width(8.dp))
 Text(
 text = report.detectedPlate?.takeIf { it.isNotEmpty() } ?: "Sin Placa",
 style = MaterialTheme.typography.titleMedium,
 fontWeight = FontWeight.Bold,
 color = if (!report.detectedPlate.isNullOrEmpty()) {
 MaterialTheme.colorScheme.primary
 } else {
 MaterialTheme.colorScheme.onSurfaceVariant
 }
 )
 }

 Surface(
 color = if (!report.detectedPlate.isNullOrEmpty()) {
 MaterialTheme.colorScheme.primaryContainer
 } else {
 MaterialTheme.colorScheme.errorContainer
 },
 shape = MaterialTheme.shapes.small
 ) {
 Text(
 text = if (!report.detectedPlate.isNullOrEmpty()) " Detectada" else "Sin detectar",
 modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
 style = MaterialTheme.typography.labelSmall,
 color = if (!report.detectedPlate.isNullOrEmpty()) {
 MaterialTheme.colorScheme.onPrimaryContainer
 } else {
 MaterialTheme.colorScheme.onErrorContainer
 }
 )
 }
 }

 Spacer(modifier = Modifier.height(12.dp))
 HorizontalDivider()
 Spacer(modifier = Modifier.height(12.dp))

 // Información resumida
 Row(
 modifier = Modifier.fillMaxWidth(),
 horizontalArrangement = Arrangement.SpaceBetween
 ) {
 Column(modifier = Modifier.weight(1f)) {
 ReportInfoRow(
 icon = Icons.Filled.AccessTime,
 label = "Fecha",
 value = dateFormat.format(Date(report.timestamp))
 )
 }

 if (report.latitude != 0.0 && report.longitude != 0.0) {
 Icon(
 imageVector = Icons.Filled.Place,
 contentDescription = "Con GPS",
 tint = MaterialTheme.colorScheme.primary,
 modifier = Modifier.size(20.dp)
 )
 }
 }

 // Indicador de que se puede hacer clic
 Spacer(modifier = Modifier.height(8.dp))
 Text(
 text = "Toca para ver detalles",
 style = MaterialTheme.typography.bodySmall,
 color = MaterialTheme.colorScheme.primary
 )
 }
 }
}

@Composable
fun ReportInfoRow(
 icon: androidx.compose.ui.graphics.vector.ImageVector,
 label: String,
 value: String
) {
 Row(
 verticalAlignment = Alignment.Top
 ) {
 Icon(
 imageVector = icon,
 contentDescription = null,
 modifier = Modifier.size(20.dp),
 tint = MaterialTheme.colorScheme.onSurfaceVariant
 )
 Spacer(modifier = Modifier.width(8.dp))
 Column {
 Text(
 text = label,
 style = MaterialTheme.typography.labelMedium,
 color = MaterialTheme.colorScheme.onSurfaceVariant
 )
 Text(
 text = value,
 style = MaterialTheme.typography.bodyMedium
 )
 }
 }
}

@Composable
fun ReportDetailsDialog(
 report: CaptureData,
 onDismiss: () -> Unit
) {
 val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())

 AlertDialog(
 onDismissRequest = onDismiss,
 icon = {
 Icon(
 imageVector = Icons.Filled.Info,
 contentDescription = null,
 tint = MaterialTheme.colorScheme.primary
 )
 },
 title = {
 Text(
 text = "Detalles del Reporte",
 style = MaterialTheme.typography.titleLarge
 )
 },
 text = {
 Column(
 modifier = Modifier
 .fillMaxWidth()
 .padding(vertical = 8.dp)
 ) {
 // Placa detectada
 DetailItem(
 icon = Icons.Filled.DirectionsCar,
 label = "Placa Detectada",
 value = report.detectedPlate?.takeIf { it.isNotEmpty() } ?: "Sin placa detectada"
 )

 Spacer(modifier = Modifier.height(12.dp))
 HorizontalDivider()
 Spacer(modifier = Modifier.height(12.dp))

 // Fecha y hora
 DetailItem(
 icon = Icons.Filled.AccessTime,
 label = "Fecha y Hora",
 value = dateFormat.format(Date(report.timestamp))
 )

 Spacer(modifier = Modifier.height(12.dp))

 // Ubicación GPS
 DetailItem(
 icon = Icons.Filled.Place,
 label = "Ubicación GPS",
 value = if (report.latitude != 0.0 && report.longitude != 0.0) {
 "Lat: ${String.format("%.6f", report.latitude)}\nLon: ${String.format("%.6f", report.longitude)}"
 } else {
 "Sin ubicación GPS"
 }
 )

 // Texto extraído (si existe)
 if (!report.extractedText.isNullOrEmpty()) {
 Spacer(modifier = Modifier.height(12.dp))
 DetailItem(
 icon = Icons.Filled.Description,
 label = "Razón del Reporte",
 value = report.extractedText
 )
 }

 // ID del reporte
 Spacer(modifier = Modifier.height(12.dp))
 DetailItem(
 icon = Icons.Filled.Fingerprint,
 label = "ID del Reporte",
 value = report.id.toString()
 )
 }
 },
 confirmButton = {
 TextButton(onClick = onDismiss) {
 Text("Cerrar")
 }
 }
 )
}

@Composable
fun DetailItem(
 icon: androidx.compose.ui.graphics.vector.ImageVector,
 label: String,
 value: String
) {
 Row(
 modifier = Modifier.fillMaxWidth(),
 verticalAlignment = Alignment.Top
 ) {
 Icon(
 imageVector = icon,
 contentDescription = null,
 modifier = Modifier.size(24.dp),
 tint = MaterialTheme.colorScheme.primary
 )
 Spacer(modifier = Modifier.width(12.dp))
 Column(modifier = Modifier.weight(1f)) {
 Text(
 text = label,
 style = MaterialTheme.typography.labelMedium,
 fontWeight = FontWeight.Bold,
 color = MaterialTheme.colorScheme.onSurfaceVariant
 )
 Spacer(modifier = Modifier.height(4.dp))
 Text(
 text = value,
 style = MaterialTheme.typography.bodyMedium,
 color = MaterialTheme.colorScheme.onSurface
 )
 }
 }
}

