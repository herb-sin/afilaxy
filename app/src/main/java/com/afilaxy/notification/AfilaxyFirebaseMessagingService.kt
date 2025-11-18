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
import com.afilaxy.performance.LogOptimizer
import com.afilaxy.presentation.emergency.EmergencyOverlayActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class AfilaxyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        LogOptimizer.d("AfilaxyFCM", "🔥 FCM RECEBIDO! Data: ${remoteMessage.data}")
        LogOptimizer.d("AfilaxyFCM", "🔥 FCM RECEBIDO! Notification: ${remoteMessage.notification}")
        
        // Handle emergency notifications
        when (remoteMessage.data["type"]) {
            "emergency_request" -> {
                LogOptimizer.d("AfilaxyFCM", "🆘 EMERGÊNCIA RECEBIDA!")
                handleEmergencyRequest(remoteMessage)
            }
            "emergency_cancelled" -> {
                LogOptimizer.d("AfilaxyFCM", "❌ EMERGÊNCIA CANCELADA!")
                handleEmergencyCancelled(remoteMessage)
            }
            "helper_response" -> {
                LogOptimizer.d("AfilaxyFCM", "✅ RESPOSTA DE HELPER RECEBIDA!")
                handleHelperResponse(remoteMessage)
            }
            else -> {
                LogOptimizer.d("AfilaxyFCM", "📱 NOTIFICAÇÃO GERAL RECEBIDA!")
                handleGeneralNotification(remoteMessage)
            }
        }
    }

    private fun handleEmergencyRequest(remoteMessage: RemoteMessage) {
        val emergencyId = remoteMessage.data["emergency_id"] ?: ""
        val requesterName = remoteMessage.data["requesterName"] ?: "Alguém"
        val distance = remoteMessage.data["distance"] ?: "próximo"
        
        LogOptimizer.d("AfilaxyFCM", "🆘 Dados: emergencyId=$emergencyId, requesterName=$requesterName, distance=$distance")
        
        // Abrir diretamente a tela vermelha (sem notificação do sistema)
        showEmergencyNotification(
            title = "🆘 Emergência de Asma",
            body = "$requesterName precisa de ajuda a ${distance}m de você",
            emergencyId = emergencyId,
            requesterName = requesterName,
            distance = distance
        )
    }

    private fun handleEmergencyCancelled(remoteMessage: RemoteMessage) {
        val emergencyId = remoteMessage.data["emergency_id"] ?: ""
        val reason = remoteMessage.data["reason"] ?: "unknown"
        
        LogOptimizer.d("AfilaxyFCM", "❌ Cancelando emergência: $emergencyId, motivo: $reason")
        
        // Cancelar notificação existente
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(emergencyId.hashCode())
        
        // Fechar tela de emergência se estiver aberta
        val cancelIntent = Intent("com.afilaxy.CANCEL_EMERGENCY").apply {
            putExtra("emergency_id", emergencyId)
            putExtra("reason", reason)
        }
        sendBroadcast(cancelIntent)
        
        LogOptimizer.d("AfilaxyFCM", "✅ Emergência cancelada com sucesso")
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

    private fun showEmergencyNotification(
        title: String,
        body: String,
        emergencyId: String,
        requesterName: String,
        distance: String
    ) {
        createNotificationChannel(EMERGENCY_CHANNEL_ID)
        
        // Criar notificação com ação direta
        val overlayIntent = Intent(this, EmergencyOverlayActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("emergency_id", emergencyId)
            putExtra("requesterName", requesterName)
            putExtra("distance", distance)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 
            System.currentTimeMillis().toInt(),
            overlayIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        LogOptimizer.d("AfilaxyFCM", "🔴 CRIANDO NOTIFICAÇÃO DE EMERGÊNCIA! emergencyId=$emergencyId")
        
        val notification = NotificationCompat.Builder(this, EMERGENCY_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(pendingIntent, true)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setOngoing(false)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(emergencyId.hashCode(), notification)
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
            putExtra("open_emergency_response", false)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
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
                    NotificationManager.IMPORTANCE_MAX
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
                if (channelId == EMERGENCY_CHANNEL_ID) {
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 1000, 500, 1000, 500, 1000)
                    setBypassDnd(true)
                    lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                    setShowBadge(true)
                }
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