package com.afilaxy.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.afilaxy.security.InputSanitizer
import androidx.core.app.NotificationCompat
import com.afilaxy.MainActivity
import com.afilaxy.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class AfilaxyFirebaseMessagingService : FirebaseMessagingService() {
    
    companion object {
        private const val TAG = "AfilaxyFCMService"
        private const val EMERGENCY_CHANNEL_ID = "afilaxy_emergency"
        private const val NOTIFICATION_ID = 1001
    }
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "🚀 AfilaxyFirebaseMessagingService criado")
        createNotificationChannel()
    }
    
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        Log.d(TAG, "🔔 NOTIFICAÇÃO RECEBIDA!")
        Log.d(TAG, "📤 From: ${InputSanitizer.sanitizeText(remoteMessage.from)}")
        Log.d(TAG, "📋 Data size: ${remoteMessage.data.size}")
        Log.d(TAG, "📢 Has notification: ${remoteMessage.notification != null}")
        
        // Verificar se é uma notificação de emergência
        val notificationType = remoteMessage.data["type"]
        if (notificationType == "emergency_alert") {
            Log.d(TAG, "🚨 Processando notificação de emergência")
            handleEmergencyNotification(remoteMessage)
        } else {
            Log.d(TAG, "ℹ️ Processando notificação geral")
            handleGeneralNotification(remoteMessage)
        }
    }
    
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "🔄 Novo token FCM recebido (${token.length} chars)")
        
        // Atualizar token no Firestore
        // Isso será feito automaticamente pelo FCMTokenManager quando o app abrir
    }
    
    private fun handleEmergencyNotification(remoteMessage: RemoteMessage) {
        val title = InputSanitizer.sanitizeText(remoteMessage.data["title"]) 
            .takeIf { it.isNotBlank() } ?: "🚨 EMERGÊNCIA AFILAXY"
        val body = InputSanitizer.sanitizeText(remoteMessage.data["body"]) 
            .takeIf { it.isNotBlank() } ?: "Alguém precisa de ajuda próximo a você!"
        val emergencyId = InputSanitizer.sanitizeText(remoteMessage.data["emergencyId"]) ?: ""
        val requesterName = InputSanitizer.sanitizeName(remoteMessage.data["requesterName"]) 
            .takeIf { it.isNotBlank() } ?: "Pessoa"
        
        Log.d(TAG, "📋 Emergency notification processed")
        Log.d(TAG, "   Has title: ${title.isNotBlank()}")
        Log.d(TAG, "   Has body: ${body.isNotBlank()}")
        Log.d(TAG, "   Has emergency ID: ${emergencyId.isNotBlank()}")
        Log.d(TAG, "   Has requester: ${requesterName.isNotBlank()}")
        
        // Criar intent para abrir o app na tela de emergência
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("emergency_id", emergencyId)
            putExtra("requester_name", requesterName)
            putExtra("notification_type", "emergency_alert")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, System.currentTimeMillis().toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Criar notificação de alta prioridade
        val notificationBuilder = NotificationCompat.Builder(this, EMERGENCY_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setVibrate(longArrayOf(0, 1000, 500, 1000))
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
        
        Log.d(TAG, "✅ Notificação de emergência exibida")
    }
    
    private fun handleGeneralNotification(remoteMessage: RemoteMessage) {
        val title = InputSanitizer.sanitizeText(remoteMessage.notification?.title) 
            .takeIf { it.isNotBlank() } ?: "Afilaxy"
        val body = InputSanitizer.sanitizeText(remoteMessage.notification?.body) 
            .takeIf { it.isNotBlank() } ?: "Nova notificação"
        
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notificationBuilder = NotificationCompat.Builder(this, EMERGENCY_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID + 1, notificationBuilder.build())
        
        Log.d(TAG, "✅ Notificação geral exibida")
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                EMERGENCY_CHANNEL_ID,
                "Emergências Afilaxy",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificações de emergência do Afilaxy"
                enableLights(true)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 1000, 500, 1000)
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            
            Log.d(TAG, "📢 Canal de notificação criado: $EMERGENCY_CHANNEL_ID")
        }
    }
}