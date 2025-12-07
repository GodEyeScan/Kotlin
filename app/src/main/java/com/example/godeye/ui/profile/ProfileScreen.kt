package com.example.godeye.ui.profile

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import android.net.Uri
import android.view.Surface
import android.view.WindowManager
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import com.example.godeye.viewmodel.AuthViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ProfileScreen(
 authViewModel: AuthViewModel,
 onLogout: () -> Unit = {},
 onNavigateToEditProfile: () -> Unit = {},
 onNavigateToAdminReports: () -> Unit = {}
) {
 val currentUser = authViewModel.currentUser.value
 val scrollState = rememberScrollState()
 var showCameraDialog by remember { mutableStateOf(false) }
 var profilePhotoUri by remember { mutableStateOf(currentUser?.profilePhotoUri ?: "") }
 val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)

 Scaffold(
 topBar = {
 TopAppBar(
 title = { Text("Mi Perfil") }
 )
 }
 ) { paddingValues ->
 Column(
 modifier = Modifier
 .fillMaxSize()
 .padding(paddingValues)
 .padding(24.dp)
 .verticalScroll(scrollState),
 horizontalAlignment = Alignment.CenterHorizontally
 ) {
 // Avatar o foto de perfil
 Box(
 modifier = Modifier
 .size(120.dp)
 .clickable {
 if (cameraPermission.status.isGranted) {
 showCameraDialog = true
 } else {
 cameraPermission.launchPermissionRequest()
 }
 }
 ) {
 if (profilePhotoUri.isNotEmpty()) {
 Image(
 painter = rememberAsyncImagePainter(profilePhotoUri),
 contentDescription = "Foto de perfil",
 modifier = Modifier
 .size(120.dp)
 .clip(CircleShape)
 .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
 contentScale = ContentScale.Crop
 )
 } else {
 Icon(
 imageVector = Icons.Filled.AccountCircle,
 contentDescription = "Perfil",
 modifier = Modifier.size(120.dp),
 tint = MaterialTheme.colorScheme.primary
 )
 }

 // Botón de cámara
 Icon(
 imageVector = Icons.Filled.CameraAlt,
 contentDescription = "Cambiar foto",
 modifier = Modifier
 .align(Alignment.BottomEnd)
 .size(36.dp)
 .clip(CircleShape)
 .border(2.dp, Color.White, CircleShape),
 tint = MaterialTheme.colorScheme.primary
 )
 }

 Spacer(modifier = Modifier.height(24.dp))

 // Información del usuario
 currentUser?.let { user ->
 Card(
 modifier = Modifier.fillMaxWidth(),
 colors = CardDefaults.cardColors(
 containerColor = MaterialTheme.colorScheme.surfaceVariant
 )
 ) {
 Column(
 modifier = Modifier.padding(16.dp),
 verticalArrangement = Arrangement.spacedBy(16.dp)
 ) {
 ProfileInfoRow(
 icon = Icons.Filled.Person,
 label = "Nombre",
 value = user.name.ifEmpty { "No especificado" }
 )
 HorizontalDivider()
 ProfileInfoRow(
 icon = Icons.Filled.Email,
 label = "Email",
 value = user.email
 )
 HorizontalDivider()
 ProfileInfoRow(
 icon = Icons.Filled.Phone,
 label = "Teléfono",
 value = if (user.phoneNumber.isNotEmpty()) {
 "${user.phonePrefix} ${user.phoneNumber}"
 } else {
 "No especificado"
 }
 )
 HorizontalDivider()
 ProfileInfoRow(
 icon = Icons.Filled.AccountCircle,
 label = "NIT",
 value = user.nit.ifEmpty { "No especificado" }
 )
 }
 }

 Spacer(modifier = Modifier.height(32.dp))

 // Botón para administradores - Ver todos los reportes
 if (user.email.lowercase() == "admin@gmail.com") {
 Card(
 modifier = Modifier.fillMaxWidth(),
 colors = CardDefaults.cardColors(
 containerColor = MaterialTheme.colorScheme.primaryContainer
 )
 ) {
 Column(
 modifier = Modifier.padding(16.dp),
 horizontalAlignment = Alignment.CenterHorizontally
 ) {
 Icon(
 imageVector = Icons.Filled.Shield,
 contentDescription = "Admin",
 modifier = Modifier.size(32.dp),
 tint = MaterialTheme.colorScheme.primary
 )
 Spacer(modifier = Modifier.height(8.dp))
 Text(
 text = "Panel de Administrador",
 style = MaterialTheme.typography.titleMedium,
 color = MaterialTheme.colorScheme.onPrimaryContainer
 )
 }
 }

 Spacer(modifier = Modifier.height(16.dp))

 Button(
 onClick = onNavigateToAdminReports,
 modifier = Modifier.fillMaxWidth(),
 colors = ButtonDefaults.buttonColors(
 containerColor = MaterialTheme.colorScheme.tertiary
 )
 ) {
 Icon(
 imageVector = Icons.Filled.Dashboard,
 contentDescription = "Ver todos los reportes",
 modifier = Modifier.size(20.dp)
 )
 Spacer(modifier = Modifier.width(8.dp))
 Text("Ver Todos los Reportes")
 }

 Spacer(modifier = Modifier.height(16.dp))
 }

 // Botón para editar datos personales
 Button(
 onClick = onNavigateToEditProfile,
 modifier = Modifier.fillMaxWidth()
 ) {
 Icon(
 imageVector = Icons.Filled.Edit,
 contentDescription = "Editar datos",
 modifier = Modifier.size(20.dp)
 )
 Spacer(modifier = Modifier.width(8.dp))
 Text("Editar Datos Personales")
 }

 Spacer(modifier = Modifier.height(16.dp))

 // Botón de cerrar sesión
 OutlinedButton(
 onClick = {
 authViewModel.logout()
 onLogout()
 },
 modifier = Modifier.fillMaxWidth(),
 colors = ButtonDefaults.outlinedButtonColors(
 contentColor = MaterialTheme.colorScheme.error
 )
 ) {
 Icon(
 imageVector = Icons.AutoMirrored.Filled.ExitToApp,
 contentDescription = "Cerrar sesión",
 modifier = Modifier.size(20.dp)
 )
 Spacer(modifier = Modifier.width(8.dp))
 Text("Cerrar Sesión")
 }
 }
 }
 }

 // Diálogo de cámara frontal
 if (showCameraDialog) {
 FrontCameraDialog(
 onDismiss = { showCameraDialog = false },
 onPhotoTaken = { uri ->
 profilePhotoUri = uri
 currentUser?.profilePhotoUri = uri
 showCameraDialog = false
 }
 )
 }
}

@Composable
fun FrontCameraDialog(
 onDismiss: () -> Unit,
 onPhotoTaken: (String) -> Unit
) {
 val context = LocalContext.current
 val lifecycleOwner = LocalLifecycleOwner.current
 var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
 var isCapturing by remember { mutableStateOf(false) }
 var errorMessage by remember { mutableStateOf<String?>(null) }
 var showNoFaceWarning by remember { mutableStateOf(true) }

 Dialog(onDismissRequest = onDismiss) {
 Card {
 Column(
 modifier = Modifier.padding(16.dp),
 horizontalAlignment = Alignment.CenterHorizontally
 ) {
 Text(
 text = "Tomar Foto de Perfil",
 style = MaterialTheme.typography.titleLarge
 )

 Spacer(modifier = Modifier.height(16.dp))

 // Advertencia si no hay rostro
 if (showNoFaceWarning) {
 Card(
 colors = CardDefaults.cardColors(
 containerColor = MaterialTheme.colorScheme.errorContainer
 ),
 modifier = Modifier.fillMaxWidth()
 ) {
 Row(
 modifier = Modifier.padding(8.dp),
 verticalAlignment = Alignment.CenterVertically
 ) {
 Icon(
 imageVector = Icons.Filled.Warning,
 contentDescription = null,
 tint = MaterialTheme.colorScheme.onErrorContainer,
 modifier = Modifier.size(20.dp)
 )
 Spacer(modifier = Modifier.width(8.dp))
 Text(
 text = "Asegúrate de que tu rostro esté visible",
 color = MaterialTheme.colorScheme.onErrorContainer,
 style = MaterialTheme.typography.bodySmall
 )
 }
 }
 Spacer(modifier = Modifier.height(8.dp))
 }

 // Vista previa de cámara frontal
 AndroidView(
 factory = { ctx ->
 val previewView = PreviewView(ctx)
 val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

 cameraProviderFuture.addListener({
 val cameraProvider = cameraProviderFuture.get()
 val preview = androidx.camera.core.Preview.Builder().build()
 preview.setSurfaceProvider(previewView.surfaceProvider)

 val imageCaptureBuilder = ImageCapture.Builder()
 imageCapture = imageCaptureBuilder.build()

 // Seleccionar cámara frontal
 val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

 try {
 cameraProvider.unbindAll()
 cameraProvider.bindToLifecycle(
 lifecycleOwner,
 cameraSelector,
 preview,
 imageCapture
 )
 } catch (e: Exception) {
 e.printStackTrace()
 }
 }, ContextCompat.getMainExecutor(ctx))

 previewView
 },
 modifier = Modifier
 .fillMaxWidth()
 .height(300.dp)
 .clip(MaterialTheme.shapes.medium)
 )

 Spacer(modifier = Modifier.height(16.dp))

 // Mensaje de error si no se detecta rostro
 if (errorMessage != null) {
 Card(
 colors = CardDefaults.cardColors(
 containerColor = MaterialTheme.colorScheme.errorContainer
 ),
 modifier = Modifier.fillMaxWidth()
 ) {
 Text(
 text = errorMessage!!,
 color = MaterialTheme.colorScheme.onErrorContainer,
 modifier = Modifier.padding(8.dp),
 style = MaterialTheme.typography.bodySmall
 )
 }
 Spacer(modifier = Modifier.height(8.dp))
 }

 Row(
 modifier = Modifier.fillMaxWidth(),
 horizontalArrangement = Arrangement.SpaceEvenly
 ) {
 OutlinedButton(onClick = onDismiss) {
 Text("Cancelar")
 }

 Button(
 onClick = {
 isCapturing = true
 errorMessage = null
 captureAndProcessPhoto(context, imageCapture) { uri ->
 isCapturing = false
 if (uri != null) {
 onPhotoTaken(uri)
 } else {
 errorMessage = "No se detectó ningún rostro. Asegúrate de que tu cara esté visible en la cámara."
 }
 }
 },
 enabled = !isCapturing
 ) {
 if (isCapturing) {
 CircularProgressIndicator(
 modifier = Modifier.size(20.dp),
 color = MaterialTheme.colorScheme.onPrimary
 )
 } else {
 Text("Capturar")
 }
 }
 }
 }
 }
 }
}

private fun captureAndProcessPhoto(
 context: Context,
 imageCapture: ImageCapture?,
 onComplete: (String?) -> Unit
) {
 val imageCapture = imageCapture ?: run {
 onComplete(null)
 return
 }

 val photoFile = File(
 context.filesDir,
 "profile_${SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.US).format(System.currentTimeMillis())}.jpg"
 )

 val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

 // Obtener orientación actual del dispositivo
 val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
 val rotation = windowManager.defaultDisplay.rotation

 imageCapture.takePicture(
 outputOptions,
 ContextCompat.getMainExecutor(context),
 object : ImageCapture.OnImageSavedCallback {
 override fun onImageSaved(output: ImageCapture.OutputFileResults) {
 // Procesar imagen con detección de rostro y rotación
 CoroutineScope(Dispatchers.IO).launch {
 try {
 val correctedFile = detectFaceAndRotate(context, photoFile, rotation)
 withContext(Dispatchers.Main) {
 onComplete(Uri.fromFile(correctedFile).toString())
 }
 } catch (e: Exception) {
 withContext(Dispatchers.Main) {
 onComplete(Uri.fromFile(photoFile).toString())
 }
 }
 }
 }

 override fun onError(exc: ImageCaptureException) {
 onComplete(null)
 }
 }
 )
}

private suspend fun detectFaceAndRotate(context: Context, photoFile: File, deviceRotation: Int): File {
 val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
 val image = InputImage.fromFilePath(context, Uri.fromFile(photoFile))

 // Configurar detector de rostros
 val options = FaceDetectorOptions.Builder()
 .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
 .setMinFaceSize(0.1f)
 .build()

 val detector = FaceDetection.getClient(options)

 try {
 val faces = detector.process(image).await()

 // Si no hay rostros detectados, lanzar excepción
 if (faces.isEmpty()) {
 detector.close()
 bitmap.recycle()
 throw Exception("No se detectó ningún rostro")
 }

 // Si hay rostro, simplemente guardamos la imagen tal cual
 // Sin transformaciones - la imagen se mantiene como fue capturada
 detector.close()
 bitmap.recycle()
 return photoFile

 } catch (e: Exception) {
 detector.close()
 bitmap.recycle()
 // Si falla la detección o no hay rostro, eliminar el archivo
 photoFile.delete()
 throw e
 }
}

@Composable
fun ProfileInfoRow(
 icon: androidx.compose.ui.graphics.vector.ImageVector,
 label: String,
 value: String
) {
 Row(
 modifier = Modifier.fillMaxWidth(),
 verticalAlignment = Alignment.CenterVertically
 ) {
 Icon(
 imageVector = icon,
 contentDescription = label,
 modifier = Modifier.size(24.dp),
 tint = MaterialTheme.colorScheme.primary
 )
 Spacer(modifier = Modifier.width(16.dp))
 Column {
 Text(
 text = label,
 style = MaterialTheme.typography.labelMedium,
 color = MaterialTheme.colorScheme.secondary
 )
 Text(
 text = value,
 style = MaterialTheme.typography.bodyLarge
 )
 }
 }
}

