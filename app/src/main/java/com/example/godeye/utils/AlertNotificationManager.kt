package com.example.godeye.utils

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import com.example.godeye.MainActivity

/**
 * Utilidad para mostrar notificaciones y alertas de placas reportadas
 *
 * @author GodEye Team
 * @version 1.0
 * @since 2025-12-07
 */
object AlertNotificationManager {

    private const val CHANNEL_ID = "plate_alerts_channel"
    private const val CHANNEL_NAME = "Alertas de Placas Reportadas"
    private const val CHANNEL_DESCRIPTION = "Notificaciones cuando se detecta una placa reportada"
    private const val NOTIFICATION_ID = 1001

    /**
     * Inicializa el canal de notificaciones (necesario para Android 8.0+)
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 250, 500, 250, 500)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Muestra una alerta de placa reportada con notificación y vibración
     *
     * @param context Contexto de la aplicación
     * @param placa Placa detectada
     * @param cantidadReportes Número de reportes de esta placa
     */
    fun showPlateAlert(context: Context, placa: String, cantidadReportes: Int) {
        // 1. Vibración
        triggerVibration(context)

        // 2. Notificación
        showNotification(context, placa, cantidadReportes)
    }

    /**
     * Activa la vibración del dispositivo con patrón de alerta
     */
    @SuppressLint("MissingPermission")
    private fun triggerVibration(context: Context) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        vibrator?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Patrón: espera 0ms, vibra 500ms, pausa 250ms, vibra 500ms, pausa 250ms, vibra 500ms
                val pattern = longArrayOf(0, 500, 250, 500, 250, 500)
                val amplitudes = intArrayOf(0, 255, 0, 255, 0, 255)
                it.vibrate(VibrationEffect.createWaveform(pattern, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                it.vibrate(longArrayOf(0, 500, 250, 500, 250, 500), -1)
            }
        }
    }

    /**
     * Muestra una notificación push sobre la placa reportada
     */
    @SuppressLint("MissingPermission")
    private fun showNotification(context: Context, placa: String, cantidadReportes: Int) {
        // Intent para abrir la app al hacer clic en la notificación
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Construir notificación
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert) // Usar icono del sistema por ahora
            .setContentTitle("⚠️ PLACA REPORTADA DETECTADA")
            .setContentText("Placa: $placa - Reportada $cantidadReportes ${if (cantidadReportes == 1) "vez" else "veces"}")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("La placa $placa ha sido reportada $cantidadReportes ${if (cantidadReportes == 1) "vez" else "veces"} en el sistema.\n\n.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 500, 250, 500, 250, 500))
            .build()

        // Mostrar notificación
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    /**
     * Cancela todas las notificaciones activas
     */
    fun cancelAllNotifications(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }
}

