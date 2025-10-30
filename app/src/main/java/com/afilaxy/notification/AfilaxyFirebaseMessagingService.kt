package com.afilaxy.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.afilaxy.MainActivity
import com.afilaxy.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class AfilaxyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        // Handle emergency notifications
        when (remoteMessage.data["type"]) {
            "emergency_request" -> handleEmergencyRequest(remoteMessage)
            "helper_response" -> handleHelperResponse(remoteMessage)
            else -> handleGeneralNotification(remoteMessage)
        }
    }

    private fun handleEmergencyRequest(remoteMessage: RemoteMessage) {
        val title = "🆘 Emergência Próxima!"
        val body = remoteMessage.data["message"] ?: "Alguém precisa de ajuda com asma"
        
        showNotification(
            title = title,
            body = body,
            channelId = EMERGENCY_CHANNEL_ID,
            priority = NotificationCompat.PRIORITY_HIGH
        )
    }

    private fun handleHelperResponse(remoteMessage: RemoteMessage) {
        val title = "✅ Helper Encontrado!"
        val body = remoteMessage.data["message"] ?: "Alguém está vindo te ajudar"
        
        showNotification(
            title = title,
            body = body,
            channelId = HELPER_CHANNEL_ID,
            priority = NotificationCompat.PRIORITY_DEFAULT
        )
    }

    private fun handleGeneralNotification(remoteMessage: RemoteMessage) {
        val title = remoteMessage.notification?.title ?: "Afilaxy"
        val body = remoteMessage.notification?.body ?: "Nova mensagem"
        
        showNotification(
            title = title,
            body = body,
            channelId = GENERAL_CHANNEL_ID,
            priority = NotificationCompat.PRIORITY_DEFAULT
        )
    }

    private fun showNotification(
        title: String,
        body: String,
        channelId: String,
        priority: Int
    ) {
        createNotificationChannel(channelId)
        
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(priority)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun createNotificationChannel(channelId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val (name, description, importance) = when (channelId) {
                EMERGENCY_CHANNEL_ID -> Triple(
                    "Emergências",
                    "Notificações de emergência de asma",
                    NotificationManager.IMPORTANCE_HIGH
                )
                HELPER_CHANNEL_ID -> Triple(
                    "Helpers",
                    "Respostas de helpers",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                else -> Triple(
                    "Geral",
                    "Notificações gerais do Afilaxy",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            }

            val channel = NotificationChannel(channelId, name, importance).apply {
                this.description = description
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Save token to Firebase for this user
        saveTokenToFirestore(token)
    }

    private fun saveTokenToFirestore(token: String) {
        // Implementation will be added when integrating with user management
    }

    companion object {
        private const val EMERGENCY_CHANNEL_ID = "emergency_channel"
        private const val HELPER_CHANNEL_ID = "helper_channel"
        private const val GENERAL_CHANNEL_ID = "general_channel"
    }
}