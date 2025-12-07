package com.example.godeye.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.MenuAnchorType
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.godeye.data.User
import com.example.godeye.utils.ValidationUtils
import com.example.godeye.viewmodel.AuthViewModel
import com.example.godeye.viewmodel.RegisterResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
 onRegisterSuccess: () -> Unit,
 onNavigateBack: () -> Unit,
 authViewModel: AuthViewModel
) {
 var name by remember { mutableStateOf("") }
 var email by remember { mutableStateOf("") }
 var password by remember { mutableStateOf("") }
 var phoneNumber by remember { mutableStateOf("") }
 var nit by remember { mutableStateOf("") }
 var selectedPrefix by remember { mutableStateOf("+57") }
 var expandedPrefix by remember { mutableStateOf(false) }
 var passwordVisible by remember { mutableStateOf(false) }

 // Estados de validación
 var nameError by remember { mutableStateOf<String?>(null) }
 var emailError by remember { mutableStateOf<String?>(null) }
 var passwordError by remember { mutableStateOf<String?>(null) }
 var phoneError by remember { mutableStateOf<String?>(null) }
 var nitError by remember { mutableStateOf<String?>(null) }
 var generalError by remember { mutableStateOf<String?>(null) }
 var showWelcomeDialog by remember { mutableStateOf(false) }

 val scrollState = rememberScrollState()
 val prefixes = ValidationUtils.getPhonePrefixes()

 Scaffold(
 topBar = {
 TopAppBar(
 title = { Text("Registro") },
 navigationIcon = {
 IconButton(onClick = onNavigateBack) {
 Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
 }
 }
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
 Text(
 text = "Crear nueva cuenta",
 style = MaterialTheme.typography.headlineMedium,
 color = MaterialTheme.colorScheme.primary
 )

 Spacer(modifier = Modifier.height(24.dp))

 // Campo Nombre
 OutlinedTextField(
 value = name,
 onValueChange = {
 name = it
 nameError = null
 generalError = null
 },
 label = { Text("Nombre completo *") },
 leadingIcon = {
 Icon(Icons.Filled.Person, contentDescription = "Nombre")
 },
 supportingText = {
 if (nameError != null) {
 Text(nameError!!)
 } else {
 Text("Mínimo 2 caracteres, solo letras")
 }
 },
 isError = nameError != null,
 singleLine = true,
 modifier = Modifier.fillMaxWidth()
 )

 Spacer(modifier = Modifier.height(16.dp))

 // Campo Email
 OutlinedTextField(
 value = email,
 onValueChange = {
 email = it
 emailError = null
 generalError = null
 },
 label = { Text("Email *") },
 leadingIcon = {
 Icon(Icons.Filled.Email, contentDescription = "Email")
 },
 supportingText = {
 if (emailError != null) {
 Text(emailError!!)
 } else {
 Text("ejemplo@dominio.com")
 }
 },
 isError = emailError != null,
 keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
 singleLine = true,
 modifier = Modifier.fillMaxWidth()
 )

 Spacer(modifier = Modifier.height(16.dp))

 // Campo Contraseña
 OutlinedTextField(
 value = password,
 onValueChange = {
 password = it
 passwordError = null
 generalError = null
 },
 label = { Text("Contraseña *") },
 leadingIcon = {
 Icon(Icons.Filled.Lock, contentDescription = "Contraseña")
 },
 trailingIcon = {
 IconButton(onClick = { passwordVisible = !passwordVisible }) {
 Icon(
 Icons.Filled.Info,
 contentDescription = if (passwordVisible) "Ocultar" else "Mostrar"
 )
 }
 },
 supportingText = {
 if (passwordError != null) {
 Text(passwordError!!)
 } else {
 Text("Mínimo 6 caracteres")
 }
 },
 isError = passwordError != null,
 visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
 keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
 singleLine = true,
 modifier = Modifier.fillMaxWidth()
 )

 Spacer(modifier = Modifier.height(16.dp))

 // Prefijo + Número de Celular
 Row(
 modifier = Modifier.fillMaxWidth(),
 horizontalArrangement = Arrangement.spacedBy(8.dp)
 ) {
 // Selector de prefijo (expandible)
 ExposedDropdownMenuBox(
 expanded = expandedPrefix,
 onExpandedChange = { expandedPrefix = !expandedPrefix },
 modifier = Modifier.width(120.dp)
 ) {
 OutlinedTextField(
 value = selectedPrefix,
 onValueChange = {},
 readOnly = true,
 label = { Text("Prefijo") },
 trailingIcon = {
 ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPrefix)
 },
 modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable)
 )

 ExposedDropdownMenu(
 expanded = expandedPrefix,
 onDismissRequest = { expandedPrefix = false }
 ) {
 prefixes.forEach { prefix ->
 DropdownMenuItem(
 text = { Text(prefix) },
 onClick = {
 selectedPrefix = prefix
 expandedPrefix = false
 }
 )
 }
 }
 }

 // Campo de número
 OutlinedTextField(
 value = phoneNumber,
 onValueChange = {
 phoneNumber = it
 phoneError = null
 generalError = null
 },
 label = { Text("Celular *") },
 leadingIcon = {
 Icon(Icons.Filled.Phone, contentDescription = "Celular")
 },
 supportingText = {
 if (phoneError != null) {
 Text(phoneError!!)
 } else {
 Text("7-15 dígitos")
 }
 },
 isError = phoneError != null,
 keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
 singleLine = true,
 modifier = Modifier.weight(1f)
 )
 }

 Spacer(modifier = Modifier.height(16.dp))

 // Campo NIT
 OutlinedTextField(
 value = nit,
 onValueChange = {
 nit = it
 nitError = null
 generalError = null
 },
 label = { Text("NIT *") },
 leadingIcon = {
 Icon(Icons.Filled.AccountCircle, contentDescription = "NIT")
 },
 supportingText = {
 if (nitError != null) {
 Text(nitError!!)
 } else {
 Text("Número de identificación tributaria")
 }
 },
 isError = nitError != null,
 keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
 singleLine = true,
 modifier = Modifier.fillMaxWidth()
 )

 // Mensaje de error general
 if (generalError != null) {
 Spacer(modifier = Modifier.height(8.dp))
 Card(
 colors = CardDefaults.cardColors(
 containerColor = MaterialTheme.colorScheme.errorContainer
 ),
 modifier = Modifier.fillMaxWidth()
 ) {
 Text(
 text = generalError!!,
 color = MaterialTheme.colorScheme.onErrorContainer,
 modifier = Modifier.padding(12.dp),
 style = MaterialTheme.typography.bodySmall
 )
 }
 }

 Spacer(modifier = Modifier.height(32.dp))

 // Botón de Registro
 Button(
 onClick = {
 // Validar todos los campos
 var hasErrors = false

 // Validar nombre
 val nameValidation = ValidationUtils.isValidName(name)
 if (!nameValidation.isValid) {
 nameError = nameValidation.message
 hasErrors = true
 }

 // Validar email
 val emailValidation = ValidationUtils.isValidEmail(email)
 if (!emailValidation.isValid) {
 emailError = emailValidation.message
 hasErrors = true
 }

 // Validar contraseña
 val passwordValidation = ValidationUtils.isValidPassword(password)
 if (!passwordValidation.isValid) {
 passwordError = passwordValidation.message
 hasErrors = true
 }

 // Validar teléfono
 val phoneValidation = ValidationUtils.isValidPhoneNumber(phoneNumber)
 if (!phoneValidation.isValid) {
 phoneError = phoneValidation.message
 hasErrors = true
 }

 // Validar NIT
 val nitValidation = ValidationUtils.isValidNIT(nit)
 if (!nitValidation.isValid) {
 nitError = nitValidation.message
 hasErrors = true
 }

 if (!hasErrors) {
 val newUser = User(
 email = email,
 password = password,
 name = name,
 phonePrefix = selectedPrefix,
 phoneNumber = phoneNumber,
 nit = nit
 )

 authViewModel.register(newUser) { result ->
 when (result) {
 is RegisterResult.Success -> {
 showWelcomeDialog = true
 }
 is RegisterResult.Error -> {
 generalError = result.message
 }
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
 Text("Registrarse")
 }
 }

 Spacer(modifier = Modifier.height(16.dp))

 Text(
 text = "* Campos obligatorios",
 style = MaterialTheme.typography.bodySmall,
 color = MaterialTheme.colorScheme.secondary
 )
 }
 }

 // Diálogo de bienvenida
 if (showWelcomeDialog) {
 AlertDialog(
 onDismissRequest = {
 showWelcomeDialog = false
 onRegisterSuccess()
 },
 title = {
 Text(
 text = "Bienvenido a GodEye",
 style = MaterialTheme.typography.headlineSmall
 )
 },
 text = {
 Text(
 text = "Hola $name, tu registro ha sido exitoso. Comienza a detectar placas ahora.",
 style = MaterialTheme.typography.bodyLarge
 )
 },
 confirmButton = {
 Button(
 onClick = {
 showWelcomeDialog = false
 onRegisterSuccess()
 }
 ) {
 Text("Comenzar")
 }
 },
 containerColor = MaterialTheme.colorScheme.surface
 )
 }
}

