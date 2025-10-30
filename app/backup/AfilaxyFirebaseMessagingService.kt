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
import com.afilaxy.security.SecureLogger
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class AfilaxyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val CHANNEL_ID = "afilaxy_emergency"
        private const val NOTIFICATION_ID = 1001
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        SecureLogger.d("FCM", "Firebase Messaging Service created")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        try {
            SecureLogger.d("FCM", "Message received from: ${remoteMessage.from}")
            
            // Handle data payload
            if (remoteMessage.data.isNotEmpty()) {
                handleDataMessage(remoteMessage.data)
            }
            
            // Handle notification payload
            remoteMessage.notification?.let {
                showNotification(it.title ?: "Afilaxy", it.body ?: "Nova mensagem")
            }
            
        } catch (e: Exception) {
            SecureLogger.e("FCM", "Error processing message", e)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        SecureLogger.d("FCM", "New token received")
        
        // Send token to server if needed
        sendTokenToServer(token)
    }

    private fun handleDataMessage(data: Map<String, String>) {
        try {
            val type = data["type"]
            val title = data["title"] ?: "Emergência"
            val body = data["body"] ?: "Solicitação de ajuda próxima"
            
            when (type) {
                "emergency_request" -> {
                    showEmergencyNotification(title, body, data)
                }
                "emergency_response" -> {
                    showResponseNotification(title, body)
                }
                else -> {
                    showNotification(title, body)
                }
            }
        } catch (e: Exception) {
            SecureLogger.e("FCM", "Error handling data message", e)
        }
    }

    private fun showEmergencyNotification(title: String, body: String, data: Map<String, String>) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("emergency_id", data["emergency_id"])
            putExtra("requester_name", data["requester_name"])
            putExtra("notification_type", "emergency")
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun showResponseNotification(title: String, body: String) {
        showNotification(title, body)
    }

    private fun showNotification(title: String, body: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Emergências Afilaxy",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificações de emergência do Afilaxy"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendTokenToServer(token: String) {
        try {
            // TODO: Implement token sending to server
            SecureLogger.d("FCM", "Token should be sent to server")
        } catch (e: Exception) {
            SecureLogger.e("FCM", "Error sending token to server", e)
        }
    }
}