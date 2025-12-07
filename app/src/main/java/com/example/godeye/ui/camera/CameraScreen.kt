package com.example.godeye.ui.camera

import android.Manifest
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.video.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.godeye.data.CaptureData
import com.example.godeye.utils.AlertNotificationManager
import com.example.godeye.utils.PlateDetector
import com.example.godeye.utils.VideoProcessor
import com.example.godeye.viewmodel.CaptureViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(
 viewModel: CaptureViewModel = viewModel(),
 authViewModel: com.example.godeye.viewmodel.AuthViewModel
) {
 val context = LocalContext.current
 val lifecycleOwner = LocalLifecycleOwner.current
 val scope = rememberCoroutineScope()
 val token = authViewModel.accessToken.value

 val permissionsState = rememberMultiplePermissionsState(
 permissions = buildList {
 add(Manifest.permission.CAMERA)
 add(Manifest.permission.RECORD_AUDIO)
 add(Manifest.permission.ACCESS_FINE_LOCATION)
 add(Manifest.permission.ACCESS_COARSE_LOCATION)
 if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
 add(Manifest.permission.POST_NOTIFICATIONS)
 }
 }
 )

 var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
 var videoCapture by remember { mutableStateOf<VideoCapture<Recorder>?>(null) }
 var isCaptureInProgress by remember { mutableStateOf(false) }
 var autoCapture by remember { mutableStateOf(true) } // Iniciar automáticamente
 var countdown by remember { mutableStateOf(5) }
 var lastCaptureStatus by remember { mutableStateOf("Iniciando captura automática...") }

 // Estados para grabación de video
 var isRecording by remember { mutableStateOf(false) }
 var recordingTime by remember { mutableStateOf(0) }
 var activeRecording by remember { mutableStateOf<Recording?>(null) }
 var currentVideoFile by remember { mutableStateOf<File?>(null) }

 // Inicializar canal de notificaciones
 LaunchedEffect(Unit) {
 AlertNotificationManager.createNotificationChannel(context)
 }

 // Observar alertas de placas reportadas
 val plateAlert = viewModel.plateAlert.value
 var showAlertDialog by remember { mutableStateOf(false) }

 LaunchedEffect(plateAlert) {
 plateAlert?.let { alert ->
 // Mostrar notificación y vibración
 AlertNotificationManager.showPlateAlert(
 context = context,
 placa = alert.placa,
 cantidadReportes = alert.cantidadReportes
 )
 // Mostrar diálogo
 showAlertDialog = true
 // Actualizar status
 lastCaptureStatus = "⚠️ PLACA ${alert.placa} REPORTADA (${alert.cantidadReportes} veces)"
 }
 }

 // Temporizador de captura automática cada 5 segundos
 LaunchedEffect(autoCapture) {
 if (autoCapture) {
 while (autoCapture) {
 countdown = 5
 while (countdown > 0 && autoCapture) {
 delay(1000)
 countdown--
 }

 if (autoCapture && !isCaptureInProgress) {
 lastCaptureStatus = "Capturando..."
 captureImageWithOCR(
 context = context,
 imageCapture = imageCapture,
 viewModel = viewModel,
 authViewModel = authViewModel,
 token = token,
 scope = scope,
 onStatusUpdate = { status -> lastCaptureStatus = status },
 onComplete = { /* Continuar automáticamente */ }
 )
 }
 }
 }
 }

 // Temporizador de grabación de video (máximo 5 segundos)
 LaunchedEffect(isRecording) {
 if (isRecording) {
 recordingTime = 0
 while (isRecording && recordingTime < 5) {
 delay(1000)
 recordingTime++
 }
 // Si llega a 5 segundos, detener automáticamente y procesar
 if (recordingTime >= 5 && isRecording) {
 activeRecording?.stop()
 isRecording = false
 lastCaptureStatus = "Procesando video..."

 // Procesar el video
 currentVideoFile?.let { videoFile ->
 scope.launch {
 try {
 val result = VideoProcessor.processVideo(
 context = context,
 videoFile = videoFile,
 onProgress = { progress ->
 val percentage = (progress * 100).toInt()
 lastCaptureStatus = "Procesando video... $percentage%"
 }
 )

 if (result.success && result.detectedPlate != null) {
 // Obtener ubicación GPS
 val locationClient = LocationServices.getFusedLocationProviderClient(context)
 var latitude = 0.0
 var longitude = 0.0

 try {
 val location = locationClient.getCurrentLocation(
 Priority.PRIORITY_HIGH_ACCURACY,
 CancellationTokenSource().token
 ).await()
 latitude = location?.latitude ?: 0.0
 longitude = location?.longitude ?: 0.0
 } catch (e: Exception) {
 Log.e("CameraScreen", "Error al obtener ubicación: ${e.message}")
 }

 // Crear captura con los datos del video
 val userEmail = authViewModel.currentUser.value?.email ?: ""
 val captureData = CaptureData(
 userEmail = userEmail,
 imageUri = "", // No guardamos el video, solo la placa
 latitude = latitude,
 longitude = longitude,
 timestamp = System.currentTimeMillis(),
 extractedText = result.allText,
 detectedPlate = result.detectedPlate
 )

 // Buscar placa en el sistema
 result.detectedPlate?.let { placa ->
 viewModel.checkPlateInSystem(token, placa) { encontrada, cantidad ->
 if (!encontrada) {
 lastCaptureStatus = "✅ Placa $placa sin reportes"
 }
 }
 }

 viewModel.addCapture(captureData, token, userEmail) { success, error ->
 if (success) {
 Log.d("CameraScreen", "Captura guardada exitosamente")
 } else {
 Log.e("CameraScreen", "Error al guardar: $error")
 }
 }

 // Eliminar archivo de video
 videoFile.delete()
 } else {
 lastCaptureStatus = "No se detectó placa en el video"
 videoFile.delete()
 }
 } catch (e: Exception) {
 Log.e("CameraScreen", "Error al procesar video: ${e.message}", e)
 lastCaptureStatus = "Error al procesar video"
 videoFile.delete()
 }
 }
 }
 }
 }
 }

 LaunchedEffect(Unit) {
 if (!permissionsState.allPermissionsGranted) {
 permissionsState.launchMultiplePermissionRequest()
 }
 }

 if (!permissionsState.allPermissionsGranted) {
 Column(
 modifier = Modifier.fillMaxSize(),
 horizontalAlignment = Alignment.CenterHorizontally,
 verticalArrangement = Arrangement.Center
 ) {
 Text("Se requieren permisos de cámara y ubicación")
 Spacer(modifier = Modifier.height(16.dp))
 Button(onClick = { permissionsState.launchMultiplePermissionRequest() }) {
 Text("Otorgar permisos")
 }
 }
 } else {
 Scaffold(
 topBar = {
 TopAppBar(
 title = { Text("GodEye - Detección de Placas") }
 )
 }
 ) { paddingValues ->
 Column(
 modifier = Modifier
 .fillMaxSize()
 .padding(paddingValues)
 .padding(16.dp),
 horizontalAlignment = Alignment.CenterHorizontally
 ) {

 // Estado de captura automática
 if (autoCapture) {
 Card(
 modifier = Modifier
 .fillMaxWidth()
 .padding(bottom = 8.dp),
 colors = CardDefaults.cardColors(
 containerColor = MaterialTheme.colorScheme.primaryContainer
 )
 ) {
 Column(
 modifier = Modifier.padding(12.dp),
 horizontalAlignment = Alignment.CenterHorizontally
 ) {
 Text(
 text = "Captura Automática Activa",
 style = MaterialTheme.typography.titleMedium,
 color = MaterialTheme.colorScheme.onPrimaryContainer
 )
 Text(
 text = "Próxima captura en: $countdown segundos",
 style = MaterialTheme.typography.bodyMedium
 )
 Text(
 text = lastCaptureStatus,
 style = MaterialTheme.typography.bodySmall,
 color = MaterialTheme.colorScheme.secondary
 )
 }
 }
 }

 // Camera Preview con borde
 Box(
 modifier = Modifier
 .fillMaxWidth()
 .weight(1f)
 .padding(4.dp) // Padding externo
 .border(3.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.medium)
 ) {
 AndroidView(
 factory = { ctx ->
 val previewView = PreviewView(ctx).apply {
 scaleType = PreviewView.ScaleType.FIT_CENTER
 }
 val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

 cameraProviderFuture.addListener({
 val cameraProvider = cameraProviderFuture.get()

 val preview = Preview.Builder().build().also {
 it.setSurfaceProvider(previewView.surfaceProvider)
 }

 imageCapture = ImageCapture.Builder()
 .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
 .build()

 // Configurar VideoCapture con Recorder
 val recorder = Recorder.Builder()
 .setQualitySelector(QualitySelector.from(Quality.HD))
 .build()
 videoCapture = VideoCapture.withOutput(recorder)

 val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

 try {
 cameraProvider.unbindAll()
 cameraProvider.bindToLifecycle(
 lifecycleOwner,
 cameraSelector,
 preview,
 imageCapture,
 videoCapture
 )
 } catch (e: Exception) {
 Log.e("CameraScreen", "Error al iniciar cámara", e)
 }
 }, ContextCompat.getMainExecutor(ctx))

 previewView
 },
 modifier = Modifier
 .fillMaxSize()
 .padding(4.dp) // Padding interno para que no toque el borde
 )
 }

 Spacer(modifier = Modifier.height(8.dp))

 // Botones de control
 Row(
 modifier = Modifier.fillMaxWidth(),
 horizontalArrangement = Arrangement.spacedBy(8.dp)
 ) {
 // Botón de toggle de captura automática
 Button(
 onClick = {
 autoCapture = !autoCapture
 if (autoCapture) {
 lastCaptureStatus = "Auto-captura iniciada"
 countdown = 5
 } else {
 lastCaptureStatus = "Auto-captura detenida"
 }
 },
 colors = if (autoCapture) {
 ButtonDefaults.buttonColors(
 containerColor = MaterialTheme.colorScheme.error
 )
 } else {
 ButtonDefaults.buttonColors()
 },
 modifier = Modifier.weight(1f),
 enabled = !isRecording
 ) {
 Text(if (autoCapture) "Detener Auto" else "Iniciar Auto")
 }

 // Botón de grabar video
 Button(
 onClick = {
 if (!isRecording) {
 // Iniciar grabación
 videoCapture?.let { capture ->
 val videoFile = File(
 context.filesDir,
 "video_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.mp4"
 )
 currentVideoFile = videoFile

 val outputOptions = FileOutputOptions.Builder(videoFile).build()

 activeRecording = capture.output
 .prepareRecording(context, outputOptions)
 .apply {
 if (ContextCompat.checkSelfPermission(
 context,
 Manifest.permission.RECORD_AUDIO
 ) == android.content.pm.PackageManager.PERMISSION_GRANTED
 ) {
 withAudioEnabled()
 }
 }
 .start(ContextCompat.getMainExecutor(context)) { recordEvent ->
 when (recordEvent) {
 is VideoRecordEvent.Start -> {
 Log.d("CameraScreen", "Grabación iniciada")
 }
 is VideoRecordEvent.Finalize -> {
 if (recordEvent.hasError()) {
 Log.e("CameraScreen", "Error en grabación: ${recordEvent.error}")
 lastCaptureStatus = "Error al grabar video"
 videoFile.delete()
 } else {
 Log.d("CameraScreen", "Grabación completada: ${videoFile.absolutePath}")
 }
 }
 }
 }

 isRecording = true
 autoCapture = false
 lastCaptureStatus = "Grabando video..."
 }
 } else {
 // Detener grabación
 activeRecording?.stop()
 activeRecording = null
 isRecording = false
 }
 },
 colors = if (isRecording) {
 ButtonDefaults.buttonColors(
 containerColor = MaterialTheme.colorScheme.error
 )
 } else {
 ButtonDefaults.buttonColors(
 containerColor = MaterialTheme.colorScheme.secondary
 )
 },
 modifier = Modifier.weight(1f)
 ) {
 if (isRecording) {
 Text("Detener ($recordingTime/5s)")
 } else {
 Text("Grabar Video")
 }
 }
 }
 }
 }
 }

 // Diálogo de alerta para placa reportada
 if (showAlertDialog && plateAlert != null) {
 AlertDialog(
 onDismissRequest = {
 showAlertDialog = false
 viewModel.clearPlateAlert()
 },
 icon = {
 Text("⚠️", style = MaterialTheme.typography.displayMedium)
 },
 title = {
 Text(
 "PLACA REPORTADA DETECTADA",
 style = MaterialTheme.typography.titleLarge,
 color = MaterialTheme.colorScheme.error
 )
 },
 text = {
 Column {
 Text(
 "Se ha detectado una placa reportada en el sistema:",
 style = MaterialTheme.typography.bodyMedium
 )
 Spacer(modifier = Modifier.height(8.dp))
 Card(
 modifier = Modifier.fillMaxWidth(),
 colors = CardDefaults.cardColors(
 containerColor = MaterialTheme.colorScheme.errorContainer
 )
 ) {
 Column(
 modifier = Modifier.padding(16.dp),
 horizontalAlignment = Alignment.CenterHorizontally
 ) {
 Text(
 plateAlert.placa,
 style = MaterialTheme.typography.headlineMedium,
 color = MaterialTheme.colorScheme.onErrorContainer
 )
 Text(
 "Reportada ${plateAlert.cantidadReportes} ${if (plateAlert.cantidadReportes == 1) "vez" else "veces"}",
 style = MaterialTheme.typography.bodyLarge,
 color = MaterialTheme.colorScheme.onErrorContainer
 )
 }
 }
 Spacer(modifier = Modifier.height(8.dp))
 Text(
 "⚠️ Proceda con precaución",
 style = MaterialTheme.typography.bodySmall,
 color = MaterialTheme.colorScheme.error
 )
 }
 },
 confirmButton = {
 Button(
 onClick = {
 showAlertDialog = false
 viewModel.clearPlateAlert()
 },
 colors = ButtonDefaults.buttonColors(
 containerColor = MaterialTheme.colorScheme.error
 )
 ) {
 Text("Entendido")
 }
 }
 )
 }
}

private fun captureImageWithOCR(
 context: Context,
 imageCapture: ImageCapture?,
 viewModel: CaptureViewModel,
 authViewModel: com.example.godeye.viewmodel.AuthViewModel,
 token: String?,
 scope: kotlinx.coroutines.CoroutineScope,
 onStatusUpdate: (String) -> Unit,
 onComplete: () -> Unit
) {
 val imageCapture = imageCapture ?: run {
 Log.e("CameraScreen", "ImageCapture no inicializado")
 onStatusUpdate("Error: Cámara no lista")
 onComplete()
 return
 }

 // Crear archivo para la imagen
 val photoFile = File(
 context.filesDir,
 SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
 .format(System.currentTimeMillis()) + ".jpg"
 )

 val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

 onStatusUpdate("Capturando imagen...")

 // Capturar imagen
 imageCapture.takePicture(
 outputOptions,
 ContextCompat.getMainExecutor(context),
 object : ImageCapture.OnImageSavedCallback {
 override fun onImageSaved(output: ImageCapture.OutputFileResults) {
 val imageUri = Uri.fromFile(photoFile).toString()

 // Procesar con OCR en coroutine
 scope.launch {
 try {
 onStatusUpdate("Procesando con OCR...")

 // Extraer texto y detectar placa
 val (extractedText, detectedPlate) = PlateDetector.processImage(imageUri)

 // Validar si debe guardarse
 if (PlateDetector.shouldSaveCapture(extractedText)) {
 onStatusUpdate("Placa detectada, obteniendo ubicación...")

 // Obtener ubicación
 val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
 val cancellationTokenSource = CancellationTokenSource()

 try {
 fusedLocationClient.getCurrentLocation(
 Priority.PRIORITY_HIGH_ACCURACY,
 cancellationTokenSource.token
 ).addOnSuccessListener { location ->
 val userEmail = authViewModel.currentUser.value?.email ?: ""
 val captureData = CaptureData(
 userEmail = userEmail,
 imageUri = imageUri,
 latitude = location?.latitude ?: 0.0,
 longitude = location?.longitude ?: 0.0,
 timestamp = System.currentTimeMillis(),
 extractedText = extractedText,
 detectedPlate = detectedPlate ?: ""
 )

 // Buscar placa en el sistema
 detectedPlate?.let { placa ->
 viewModel.checkPlateInSystem(token, placa) { encontrada, cantidad ->
 if (!encontrada) {
 onStatusUpdate("✅ Placa $placa sin reportes")
 }
 }
 }

 viewModel.addCapture(captureData, token, userEmail) { success, error ->
 if (success) {
 onStatusUpdate("Placa guardada: ${detectedPlate ?: "detectada"}")
 } else {
 onStatusUpdate("Error: $error")
 }
 }
 Log.d("CameraScreen", "Captura guardada con placa: $detectedPlate")
 onComplete()
 }.addOnFailureListener { e ->
 Log.e("CameraScreen", "Error al obtener ubicación", e)
 // Guardar sin ubicación
 val userEmail = authViewModel.currentUser.value?.email ?: ""
 val captureData = CaptureData(
 userEmail = userEmail,
 imageUri = imageUri,
 latitude = 0.0,
 longitude = 0.0,
 timestamp = System.currentTimeMillis(),
 extractedText = extractedText,
 detectedPlate = detectedPlate ?: ""
 )

 // Buscar placa en el sistema
 detectedPlate?.let { placa ->
 viewModel.checkPlateInSystem(token, placa) { encontrada, _ ->
 if (!encontrada) {
 onStatusUpdate("✅ Placa $placa sin reportes")
 }
 }
 }

 viewModel.addCapture(captureData, token, userEmail) { success, error ->
 if (success) {
 onStatusUpdate("Placa guardada sin GPS")
 } else {
 onStatusUpdate("Error: $error")
 }
 }
 onComplete()
 }
 } catch (e: SecurityException) {
 Log.e("CameraScreen", "Sin permisos de ubicación", e)
 onStatusUpdate("Guardado sin ubicación")
 onComplete()
 }
 } else {
 // Eliminar imagen si no hay placa
 photoFile.delete()
 onStatusUpdate("Ignorado: no se detectó placa")
 Log.d("CameraScreen", "Captura ignorada: no se detectó placa válida")
 onComplete()
 }

 } catch (e: Exception) {
 Log.e("CameraScreen", "Error en procesamiento OCR", e)
 photoFile.delete()
 onStatusUpdate("Error en OCR")
 onComplete()
 }
 }
 }

 override fun onError(exc: ImageCaptureException) {
 Log.e("CameraScreen", "Error al capturar imagen: ${exc.message}", exc)
 onStatusUpdate("Error al capturar")
 onComplete()
 }
 }
 )
}

