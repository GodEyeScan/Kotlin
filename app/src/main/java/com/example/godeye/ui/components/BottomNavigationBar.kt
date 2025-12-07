package com.example.godeye.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

sealed /**
 * BottomNavItem
 *
 * Descripción: [Agregar descripción]
 *
 * @author GodEye Team
 * @version 1.0
 * @since 2025-12-07
 */
class BottomNavItem(
 val route: String,
 val icon: ImageVector,
 val label: String
) {
 object Camera : BottomNavItem("camera", Icons.Filled.CameraAlt, "Cámara")
 object History : BottomNavItem("captureList", Icons.Filled.History, "Historial")
 object Profile : BottomNavItem("profile", Icons.Filled.Person, "Perfil")
 object Report : BottomNavItem("report", Icons.Filled.Report, "Reporte")
 object Logout : BottomNavItem("logout", Icons.AutoMirrored.Filled.ExitToApp, "Salir")
}

@Composable
fun BottomNavigationBar(
 currentRoute: String?,
 onNavigate: (String) -> Unit
) {
 val items = listOf(
 BottomNavItem.Camera,
 BottomNavItem.History,
 BottomNavItem.Profile,
 BottomNavItem.Report,
 BottomNavItem.Logout
 )

 NavigationBar {
 items.forEach { item ->
 // Si estamos en adminReports, resaltar el botón de Reporte
 val isSelected = when {
 currentRoute == "adminReports" && item.route == "report" -> true
 else -> currentRoute == item.route
 }

 NavigationBarItem(
 icon = { Icon(item.icon, contentDescription = item.label) },
 label = { Text(item.label) },
 selected = isSelected,
 onClick = { onNavigate(item.route) }
 )
 }
 }
}

