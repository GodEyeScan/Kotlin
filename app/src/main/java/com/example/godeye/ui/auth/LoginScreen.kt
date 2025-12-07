package com.example.godeye.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.godeye.viewmodel.AuthViewModel
import com.example.godeye.viewmodel.LoginResult
import com.example.godeye.ui.components.LogoImage

@Composable
fun LoginScreen(
 onLoginSuccess: () -> Unit,
 onNavigateToRegister: () -> Unit,
 authViewModel: AuthViewModel
) {
 var email by remember { mutableStateOf("") }
 var password by remember { mutableStateOf("") }
 var passwordVisible by remember { mutableStateOf(false) }
 var errorMessage by remember { mutableStateOf<String?>(null) }
 var showWelcomeDialog by remember { mutableStateOf(false) }
 var userName by remember { mutableStateOf("") }
 var loginSuccessful by remember { mutableStateOf(false) }

 // Observar cuando el perfil se cargue después del login
 LaunchedEffect(authViewModel.currentUser.value?.name, loginSuccessful) {
 if (loginSuccessful && authViewModel.currentUser.value != null) {
 kotlinx.coroutines.delay(200) // Pequeño delay para asegurar que el perfil se cargó
 userName = authViewModel.currentUser.value?.name ?: authViewModel.currentUser.value?.email?.substringBefore("@") ?: "Usuario"
 showWelcomeDialog = true
 loginSuccessful = false // Reset
 }
 }

 Column(
 modifier = Modifier
 .fillMaxSize()
 .padding(24.dp)
 .verticalScroll(rememberScrollState()),
 horizontalAlignment = Alignment.CenterHorizontally,
 verticalArrangement = Arrangement.Center
 ) {
 // Logo personalizado de la aplicación
 LogoImage(
 size = 140.dp,
 removeWhiteBackground = true
 )

 Spacer(modifier = Modifier.height(24.dp))

 // Título
 Text(
 text = "GodEye",
 style = MaterialTheme.typography.headlineLarge,
 color = MaterialTheme.colorScheme.primary
 )

 Text(
 text = "Detección de Placas",
 style = MaterialTheme.typography.titleMedium,
 color = MaterialTheme.colorScheme.secondary
 )

 Spacer(modifier = Modifier.height(48.dp))

 // Campo de email
 OutlinedTextField(
 value = email,
 onValueChange = {
 email = it
 errorMessage = null
 },
 label = { Text("Email") },
 leadingIcon = {
 Icon(Icons.Filled.Email, contentDescription = "Email")
 },
 keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
 singleLine = true,
 modifier = Modifier.fillMaxWidth(),
 isError = errorMessage != null
 )

 Spacer(modifier = Modifier.height(16.dp))

 // Campo de contraseña
 OutlinedTextField(
 value = password,
 onValueChange = {
 password = it
 errorMessage = null
 },
 label = { Text("Contraseña") },
 leadingIcon = {
 Icon(Icons.Filled.Lock, contentDescription = "Contraseña")
 },
 trailingIcon = {
 IconButton(onClick = { passwordVisible = !passwordVisible }) {
 Icon(
 Icons.Filled.Info,
 contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
 )
 }
 },
 visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
 keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
 singleLine = true,
 modifier = Modifier.fillMaxWidth(),
 isError = errorMessage != null
 )

 // Mensaje de error
 if (errorMessage != null) {
 Spacer(modifier = Modifier.height(8.dp))
 Text(
 text = errorMessage!!,
 color = MaterialTheme.colorScheme.error,
 style = MaterialTheme.typography.bodySmall,
 modifier = Modifier.fillMaxWidth(),
 textAlign = TextAlign.Start
 )
 }

 Spacer(modifier = Modifier.height(32.dp))

 // Botón de Iniciar Sesión
 Button(
 onClick = {
 if (email.isBlank() || password.isBlank()) {
 errorMessage = "Por favor completa todos los campos"
 return@Button
 }

 authViewModel.login(email, password) { result ->
 when (result) {
 is LoginResult.Success -> {
 loginSuccessful = true
 }
 is LoginResult.Error -> {
 errorMessage = result.message
 }
 }
 }
 },
 modifier = Modifier
 .fillMaxWidth()
 .height(50.dp),
 enabled = !authViewModel.isLoading.value
 ) {
 if (authViewModel.isLoading.value) {
 CircularProgressIndicator(
 modifier = Modifier.size(24.dp),
 color = MaterialTheme.colorScheme.onPrimary
 )
 } else {
 Text("Iniciar Sesión")
 }
 }

 Spacer(modifier = Modifier.height(16.dp))

 // Botón de Registro
 OutlinedButton(
 onClick = onNavigateToRegister,
 modifier = Modifier
 .fillMaxWidth()
 .height(50.dp),
 enabled = !authViewModel.isLoading.value
 ) {
 Text("Registrarse")
 }
 }

 // Diálogo de bienvenida
 if (showWelcomeDialog) {
 AlertDialog(
 onDismissRequest = {
 showWelcomeDialog = false
 onLoginSuccess()
 },
 title = {
 Text(
 text = "Bienvenido a GodEye",
 style = MaterialTheme.typography.headlineSmall
 )
 },
 text = {
 Text(
 text = "Hola $userName, has iniciado sesion exitosamente.",
 style = MaterialTheme.typography.bodyLarge
 )
 },
 confirmButton = {
 Button(
 onClick = {
 showWelcomeDialog = false
 onLoginSuccess()
 }
 ) {
 Text("Continuar")
 }
 },
 containerColor = MaterialTheme.colorScheme.surface
 )
 }
}

