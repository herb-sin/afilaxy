package com.afilaxy.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import com.afilaxy.MainActivity
import com.afilaxy.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val isEmergency = remoteMessage.data["type"] == "emergency_alert"
        
        if (isEmergency) {
            val requesterName = remoteMessage.data["requesterName"] ?: "Alguém"
            sendEmergencyNotification(
                "🚨 EMERGÊNCIA AFILAXY",
                "$requesterName precisa de bombinha próximo a você!"
            )
        } else {
            remoteMessage.notification?.let {
                sendNotification(it.title ?: "Afilaxy", it.body ?: "")
            }
        }
    }

    private fun sendEmergencyNotification(title: String, message: String) {
        try {
            val channelId = "afilaxy_emergency"
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return

        // Canal de emergência com máxima prioridade
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val emergencySound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val channel = NotificationChannel(
                channelId,
                "Emergências Afilaxy",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificações críticas de emergência"
                setSound(emergencySound, audioAttributes)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 1000, 500, 1000, 500, 1000)
                setBypassDnd(true) // Bypass "Não Perturbe"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Vibração intensa
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        vibrator?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                it.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 1000, 500, 1000), -1))
            } else {
                it.vibrate(longArrayOf(0, 1000, 500, 1000), -1)
            }
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("open_emergency", true)
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(pendingIntent, true) // Acende a tela
            .addAction(R.drawable.ic_notification, "ACEITAR AJUDA", pendingIntent)
            .build()

        notificationManager.notify(generateNotificationId(), notification)
        } catch (e: SecurityException) {
            android.util.Log.e("MyFirebaseMessagingService", "Permissão negada para notificação: ${e.message}")
        } catch (e: Exception) {
            android.util.Log.e("MyFirebaseMessagingService", "Erro ao enviar notificação de emergência: ${e.message}")
        }
    }
    
    private fun generateNotificationId(): Int {
        return (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
    }

    private fun sendNotification(title: String, message: String) {
        val channelId = "afilaxy_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Afilaxy Notificações",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(0, notification)
    }
}