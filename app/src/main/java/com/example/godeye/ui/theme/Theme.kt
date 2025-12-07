/**
 * Theme.kt
 *
 * Definición del tema (colores, tipografías) usado por la app.
 */

package com.example.godeye.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
 primary = Primary,
 onPrimary = TextLight,
 primaryContainer = PrimaryVariant,
 onPrimaryContainer = TextLight,

 secondary = Secondary,
 onSecondary = TextLight,
 secondaryContainer = SecondaryVariant,
 onSecondaryContainer = TextLight,

 tertiary = Accent,
 onTertiary = TextLight,
 tertiaryContainer = AccentLight,
 onTertiaryContainer = TextPrimary,

 background = BackgroundDark,
 onBackground = TextLight,

 surface = SurfaceDark,
 onSurface = TextLight,
 surfaceVariant = SurfaceVariantDark,
 onSurfaceVariant = TextLight,

 error = Error,
 onError = TextLight
)

private val LightColorScheme = lightColorScheme(
 primary = Primary,
 onPrimary = TextLight,
 primaryContainer = PrimaryLight,
 onPrimaryContainer = TextPrimary,

 secondary = Secondary,
 onSecondary = TextLight,
 secondaryContainer = SecondaryLight,
 onSecondaryContainer = TextPrimary,

 tertiary = Accent,
 onTertiary = TextLight,
 tertiaryContainer = AccentLight,
 onTertiaryContainer = TextPrimary,

 background = BackgroundLight,
 onBackground = TextPrimary,

 surface = SurfaceLight,
 onSurface = TextPrimary,
 surfaceVariant = SurfaceVariant,
 onSurfaceVariant = TextPrimary,

 error = Error,
 onError = TextLight
)

@Composable
fun GodEyeTheme(
 darkTheme: Boolean = isSystemInDarkTheme(),
 // Dynamic color deshabilitado para usar nuestra paleta personalizada
 dynamicColor: Boolean = false,
 content: @Composable () -> Unit
) {
 val colorScheme = when {
 dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
 val context = LocalContext.current
 if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
 }
 darkTheme -> DarkColorScheme
 else -> LightColorScheme
 }

 val view = LocalView.current
 if (!view.isInEditMode) {
 SideEffect {
 val window = (view.context as Activity).window
 window.statusBarColor = colorScheme.primary.toArgb()
 WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
 }
 }

 MaterialTheme(
 colorScheme = colorScheme,
 typography = Typography,
 content = content
 )
}