package com.example.godeye

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.godeye.ui.auth.LoginScreen
import com.example.godeye.ui.auth.RegisterScreen
import com.example.godeye.ui.camera.CameraScreen
import com.example.godeye.ui.captures.CaptureListScreen
import com.example.godeye.ui.components.BottomNavigationBar
import com.example.godeye.ui.map.MapScreen
import com.example.godeye.ui.profile.ProfileScreen
import com.example.godeye.ui.report.ReportScreen
import com.example.godeye.ui.theme.GodEyeTheme
import com.example.godeye.viewmodel.AuthViewModel
import com.example.godeye.viewmodel.CaptureViewModel

/**
 * MainActivity
 *
 * Descripción: [Agregar descripción]
 *
 * @author GodEye Team
 * @version 1.0
 * @since 2025-12-07
 */
class MainActivity : ComponentActivity() {
 override fun onCreate(savedInstanceState: Bundle?) {
 super.onCreate(savedInstanceState)
 enableEdgeToEdge()
 setContent {
 GodEyeTheme {
 Surface(
 modifier = Modifier.fillMaxSize(),
 color = MaterialTheme.colorScheme.background
 ) {
 GodEyeApp()
 }
 }
 }
 }
}

@Composable
fun GodEyeApp() {
 val navController = rememberNavController()
 val captureViewModel: CaptureViewModel = viewModel()
 val authViewModel: AuthViewModel = viewModel()

 // Obtener la ruta actual
 val navBackStackEntry by navController.currentBackStackEntryAsState()
 val currentRoute = navBackStackEntry?.destination?.route

 // Rutas que deben mostrar la barra de navegación inferior
 val bottomNavRoutes = listOf("camera", "captureList", "profile", "report", "adminReports")
 val showBottomBar = currentRoute in bottomNavRoutes

 Scaffold(
 bottomBar = {
 if (showBottomBar) {
 BottomNavigationBar(
 currentRoute = currentRoute,
 onNavigate = { route ->
 if (route == "logout") {
 // Manejar logout
 authViewModel.logout()
 captureViewModel.clearUserData() // Limpiar datos del ViewModel
 navController.navigate("login") {
 popUpTo(0) { inclusive = true }
 }
 } else {
 navController.navigate(route) {
 // Evitar múltiples copias de la misma pantalla
 popUpTo(navController.graph.startDestinationId) {
 saveState = true
 }
 // Restaurar estado al regresar
 launchSingleTop = true
 restoreState = true
 }
 }
 }
 )
 }
 }
 ) { paddingValues ->
 NavHost(
 navController = navController,
 startDestination = "login",
 modifier = Modifier.padding(paddingValues)
 ) {
 // Pantalla de Login
 composable("login") {
 LoginScreen(
 onLoginSuccess = {
 // Establecer el usuario actual en el ViewModel de capturas
 authViewModel.currentUser.value?.email?.let { email ->
 captureViewModel.setCurrentUser(email)
 }
 navController.navigate("camera") {
 popUpTo("login") { inclusive = true }
 }
 },
 onNavigateToRegister = { navController.navigate("register") },
 authViewModel = authViewModel
 )
 }

 // Pantalla de Registro
 composable("register") {
 RegisterScreen(
 onRegisterSuccess = {
 // Establecer el usuario actual en el ViewModel de capturas
 authViewModel.currentUser.value?.email?.let { email ->
 captureViewModel.setCurrentUser(email)
 }
 navController.navigate("camera") {
 popUpTo("register") { inclusive = true }
 }
 },
 onNavigateBack = { navController.popBackStack() },
 authViewModel = authViewModel
 )
 }

 // Pantalla de Cámara (con barra inferior)
 composable("camera") {
 CameraScreen(
 viewModel = captureViewModel,
 authViewModel = authViewModel
 )
 }

 // Pantalla de Historial/Lista de Capturas (con barra inferior)
 composable("captureList") {
 CaptureListScreen(
 onNavigateBack = { navController.popBackStack() },
 viewModel = captureViewModel,
 authViewModel = authViewModel,
 onCaptureClick = { capture ->
 // Guardar capture enViewModel para pasarla a MapScreen
 navController.currentBackStackEntry?.savedStateHandle?.set("capture", capture)
 navController.navigate("map")
 }
 )
 }

 // Pantalla de Perfil (con barra inferior)
 composable("profile") {
 ProfileScreen(
 authViewModel = authViewModel,
 onLogout = {
 navController.navigate("login") {
 popUpTo(0) { inclusive = true }
 }
 },
 onNavigateToEditProfile = {
 navController.navigate("userProfile")
 },
 onNavigateToAdminReports = {
 navController.navigate("adminReports")
 }
 )
 }

 // Pantalla de Edición de Perfil (datos locales)
 composable("userProfile") {
 com.example.godeye.ui.profile.UserProfileScreen(
 authViewModel = authViewModel,
 onNavigateBack = { navController.popBackStack() }
 )
 }

 // Pantalla de Reporte (con barra inferior)
 composable("report") {
 ReportScreen(
 authViewModel = authViewModel,
 captureViewModel = captureViewModel,
 onNavigateToAdminReports = {
 navController.navigate("adminReports")
 }
 )
 }

 // Pantalla de Mapa
 composable("map") {
 val capture = navController.previousBackStackEntry?.savedStateHandle?.get<com.example.godeye.data.CaptureData>("capture")
 capture?.let {
 MapScreen(
 capture = it,
 onNavigateBack = { navController.popBackStack() }
 )
 }
 }

 // Pantalla de Reportes de Administrador
 composable("adminReports") {
 com.example.godeye.ui.admin.AdminReportsScreen(
 authViewModel = authViewModel,
 captureViewModel = captureViewModel,
 onBack = { navController.popBackStack() }
 )
 }
 }
 }
}
