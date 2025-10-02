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
import com.afilaxy.security.AuthValidator
import com.afilaxy.security.InputSanitizer
import com.afilaxy.utils.ErrorHandler
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {
    
    companion object {
        private const val EMERGENCY_CHANNEL_ID = "afilaxy_emergency"
        private const val DEFAULT_CHANNEL_ID = "afilaxy_channel"
        private const val EMERGENCY_TYPE = "emergency_alert"
    }
    
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        ErrorHandler.safeCall(
            operation = "onMessageReceived",
            onError = { error ->
                android.util.Log.e("MessagingService", "Erro ao processar mensagem: ${error.logMessage}")
            }
        ) {
            when (remoteMessage.data["type"]) {
                EMERGENCY_TYPE -> handleEmergencyMessage(remoteMessage)
                else -> handleRegularMessage(remoteMessage)
            }
        }
    }
    
    private fun handleEmergencyMessage(remoteMessage: RemoteMessage) {
        if (!AuthValidator.isUserAuthenticated()) {
            android.util.Log.w("MessagingService", "Usuário não autenticado - ignorando notificação")
            return
        }
        
        val requesterName = InputSanitizer.sanitizeText(remoteMessage.data["requesterName"]) ?: "Alguém"
        sendEmergencyNotification(
            "🚨 EMERGÊNCIA AFILAXY",
            "$requesterName precisa de bombinha próximo a você!"
        )
    }
    
    private fun handleRegularMessage(remoteMessage: RemoteMessage) {
        if (!AuthValidator.isUserAuthenticated()) {
            android.util.Log.w("MessagingService", "Usuário não autenticado - ignorando notificação")
            return
        }
        
        remoteMessage.notification?.let { notification ->
            val title = InputSanitizer.sanitizeText(notification.title) ?: "Afilaxy"
            val body = InputSanitizer.sanitizeText(notification.body) ?: ""
            sendNotification(title, body)
        }
    }

    private fun sendEmergencyNotification(title: String, message: String) {
        ErrorHandler.safeCall(
            operation = "sendEmergencyNotification",
            onError = { error ->
                android.util.Log.e("MessagingService", "Falha na notificação de emergência: ${error.logMessage}")
            }
        ) {
            val notificationManager = getNotificationManager() ?: return@safeCall
            
            createEmergencyChannel(notificationManager)
            triggerEmergencyVibration()
            
            val pendingIntent = createEmergencyIntent()
            val notification = buildEmergencyNotification(title, message, pendingIntent)
            
            notificationManager.notify(generateNotificationId(), notification)
        }
    }
    
    private fun createEmergencyChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val emergencySound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val channel = NotificationChannel(
                EMERGENCY_CHANNEL_ID,
                "Emergências Afilaxy",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificações críticas de emergência"
                setSound(emergencySound, audioAttributes)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 1000, 500, 1000, 500, 1000)
                setBypassDnd(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun triggerEmergencyVibration() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        val vibrationPattern = longArrayOf(0, 1000, 500, 1000)
        
        vibrator?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                it.vibrate(VibrationEffect.createWaveform(vibrationPattern, -1))
            } else {
                it.vibrate(vibrationPattern, -1)
            }
        }
    }
    
    private fun createEmergencyIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("open_emergency", true)
        }
        return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    }
    
    private fun buildEmergencyNotification(
        title: String,
        message: String,
        pendingIntent: PendingIntent
    ) = NotificationCompat.Builder(this, EMERGENCY_CHANNEL_ID)
        .setContentTitle(title)
        .setContentText(message)
        .setSmallIcon(R.drawable.ic_notification)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
        .setPriority(NotificationCompat.PRIORITY_MAX)
        .setCategory(NotificationCompat.CATEGORY_ALARM)
        .setFullScreenIntent(pendingIntent, true)
        .addAction(R.drawable.ic_notification, "ACEITAR AJUDA", pendingIntent)
        .build()
    
    private fun generateNotificationId(): Int {
        return (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
    }

    private fun sendNotification(title: String, message: String) {
        ErrorHandler.safeCall(
            operation = "sendNotification",
            onError = { error ->
                android.util.Log.e("MessagingService", "Falha na notificação: ${error.logMessage}")
            }
        ) {
            val notificationManager = getNotificationManager() ?: return@safeCall
            
            createDefaultChannel(notificationManager)
            
            val intent = Intent(this, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
            
            val notification = NotificationCompat.Builder(this, DEFAULT_CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()
            
            notificationManager.notify(generateNotificationId(), notification)
        }
    }
    
    private fun getNotificationManager(): NotificationManager? {
        return getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
    }
    
    private fun createDefaultChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                DEFAULT_CHANNEL_ID,
                "Afilaxy Notificações",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }
    }
}