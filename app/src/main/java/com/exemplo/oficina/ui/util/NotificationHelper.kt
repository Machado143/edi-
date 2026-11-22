package com.exemplo.oficina.ui.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.exemplo.oficina.R
import com.exemplo.oficina.data.local.SettingsRepository
import kotlinx.coroutines.flow.first

class NotificationHelper(
    private val context: Context,
    private val settingsRepository: SettingsRepository
) {
    companion object {
        const val CHANNEL_ID = "os_status"
        const val CHANNEL_NAME = "Status das OS"
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    @android.annotation.SuppressLint("MissingPermission")
    suspend fun notificarMudancaStatus(osId: Int, novoStatus: String) {
        val enabled = settingsRepository.notificationsEnabledFlow.first()
        if (!enabled) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val novoStatusLegivel = when (novoStatus) {
            "ABERTA" -> "Aberta"
            "EM_ANDAMENTO" -> "Em andamento"
            "FINALIZADA" -> "Finalizada"
            "CANCELADA" -> "Cancelada"
            else -> novoStatus
        }

        createNotificationChannel()

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_os)
            .setContentTitle("OS #$osId")
            .setContentText("Status alterado para $novoStatusLegivel")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(context).notify(osId, builder.build())
        } catch (_: SecurityException) {
            // Permissão revogada em tempo de execução: ignora
        }
    }
}
