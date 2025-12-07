package com.example.godeye

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

/**
 * Application Class con Hilt
 * Inicializa dependencias globales y logging estructurado
 */
@HiltAndroidApp
/**
 * GodEyeApplication
 *
 * Descripción: [Agregar descripción]
 *
 * @author GodEye Team
 * @version 1.0
 * @since 2025-12-07
 */
class GodEyeApplication : Application() {

 override fun onCreate() {
 super.onCreate()

 // Inicializar Timber para logging estructurado
 if (BuildConfig.DEBUG) {
 // En desarrollo: Log detallado con ubicación del código
 Timber.plant(object : Timber.DebugTree() {
 override fun createStackElementTag(element: StackTraceElement): String {
 return "(${element.fileName}:${element.lineNumber})#${element.methodName}"
 }
 })
 Timber.d("GodEye Application iniciada en modo DEBUG")
 } else {
 // En producción: Log básico sin información sensible
 Timber.plant(ReleaseTree())
 Timber.i("GodEye Application iniciada en modo RELEASE")
 }

 Timber.i("Inicialización de GodEye completada")
 }
}

/**
 * Custom Timber Tree para producción
 * Solo registra errores y eventos críticos
 */
class ReleaseTree : Timber.Tree() {
 override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
 if (priority == android.util.Log.ERROR || priority == android.util.Log.WARN) {
 // Aquí se podría integrar con un servicio de crash reporting
 // como Firebase Crashlytics, Sentry, etc.

 // Por ahora solo log del sistema
 android.util.Log.println(priority, tag ?: "GodEye", message)
 t?.let { android.util.Log.println(priority, tag ?: "GodEye", it.stackTraceToString()) }
 }
 }
}

