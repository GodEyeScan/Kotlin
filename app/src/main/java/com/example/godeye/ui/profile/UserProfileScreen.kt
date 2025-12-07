package com.example.godeye.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.godeye.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
 authViewModel: AuthViewModel,
 onNavigateBack: () -> Unit
) {
 val userProfile = authViewModel.userProfile.value
 val currentUser = authViewModel.currentUser.value

 var name by remember { mutableStateOf(userProfile?.name ?: "") }
 var phone by remember { mutableStateOf(userProfile?.phone ?: "") }
 var nit by remember { mutableStateOf(userProfile?.nit ?: "") }
 var showSuccessMessage by remember { mutableStateOf(false) }

 Scaffold(
 topBar = {
 TopAppBar(
 title = { Text("Mi Perfil") },
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
 .padding(16.dp),
 verticalArrangement = Arrangement.spacedBy(16.dp)
 ) {
 // Información de la cuenta (solo lectura)
 Card(
 modifier = Modifier.fillMaxWidth(),
 colors = CardDefaults.cardColors(
 containerColor = MaterialTheme.colorScheme.primaryContainer
 )
 ) {
 Column(
 modifier = Modifier.padding(16.dp),
 verticalArrangement = Arrangement.spacedBy(8.dp)
 ) {
 Text(
 text = "Información de la Cuenta",
 style = MaterialTheme.typography.titleMedium,
 fontWeight = FontWeight.Bold
 )
 Text(
 text = "Email: ${currentUser?.email ?: "No disponible"}",
 style = MaterialTheme.typography.bodyMedium
 )
 if (authViewModel.isAdmin.value) {
 Text(
 text = "Rol: Administrador",
 style = MaterialTheme.typography.bodyMedium,
 color = MaterialTheme.colorScheme.primary,
 fontWeight = FontWeight.Bold
 )
 }
 }
 }

 Spacer(modifier = Modifier.height(8.dp))

 // Información personal (editable, guardada localmente)
 Text(
 text = "Datos Personales (guardados localmente)",
 style = MaterialTheme.typography.titleMedium,
 fontWeight = FontWeight.Bold
 )

 OutlinedTextField(
 value = name,
 onValueChange = { name = it },
 label = { Text("Nombre Completo") },
 modifier = Modifier.fillMaxWidth(),
 singleLine = true
 )

 OutlinedTextField(
 value = phone,
 onValueChange = { phone = it },
 label = { Text("Teléfono") },
 modifier = Modifier.fillMaxWidth(),
 singleLine = true
 )

 OutlinedTextField(
 value = nit,
 onValueChange = { nit = it },
 label = { Text("NIT") },
 modifier = Modifier.fillMaxWidth(),
 singleLine = true
 )

 Spacer(modifier = Modifier.height(8.dp))

 // Botón guardar
 Button(
 onClick = {
 authViewModel.saveUserProfile(name, phone, nit)
 showSuccessMessage = true
 },
 modifier = Modifier.fillMaxWidth(),
 enabled = name.isNotBlank() || phone.isNotBlank() || nit.isNotBlank()
 ) {
 Text("Guardar Información")
 }

 // Mensaje de éxito
 if (showSuccessMessage) {
 Card(
 modifier = Modifier.fillMaxWidth(),
 colors = CardDefaults.cardColors(
 containerColor = MaterialTheme.colorScheme.secondaryContainer
 )
 ) {
 Text(
 text = " Información guardada correctamente",
 modifier = Modifier.padding(16.dp),
 style = MaterialTheme.typography.bodyMedium
 )
 }

 LaunchedEffect(Unit) {
 kotlinx.coroutines.delay(3000)
 showSuccessMessage = false
 }
 }

 Spacer(modifier = Modifier.weight(1f))

 // Información
 Card(
 modifier = Modifier.fillMaxWidth(),
 colors = CardDefaults.cardColors(
 containerColor = MaterialTheme.colorScheme.surfaceVariant
 )
 ) {
 Column(
 modifier = Modifier.padding(16.dp),
 verticalArrangement = Arrangement.spacedBy(8.dp)
 ) {
 Text(
 text = "ℹ Información Importante",
 style = MaterialTheme.typography.titleSmall,
 fontWeight = FontWeight.Bold
 )
 Text(
 text = "• Estos datos se guardan solo en este dispositivo\n" +
 "• No se comparten con la nube\n" +
 "• Se utilizan para tus reportes locales",
 style = MaterialTheme.typography.bodySmall
 )
 }
 }
 }
 }
}

