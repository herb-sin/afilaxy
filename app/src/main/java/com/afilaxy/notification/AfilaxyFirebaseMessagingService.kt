package com.afilaxy.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.afilaxy.security.InputSanitizer
import com.afilaxy.security.SecureLogger
import com.afilaxy.security.SqlInjectionPrevention
import com.afilaxy.security.ErrorHandler
import androidx.core.app.NotificationCompat
import com.afilaxy.MainActivity
import com.afilaxy.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Firebase Cloud Messaging service for Afilaxy
 * Handles emergency notifications with security measures
 * 
 * Security features:
 * - Input sanitization to prevent log injection
 * - Secure logging without sensitive data exposure
 * - Comprehensive error handling
 */
class AfilaxyFirebaseMessagingService : FirebaseMessagingService() {
    
    companion object {
        private const val TAG = "AfilaxyFCMService"
        private const val EMERGENCY_CHANNEL_ID = "afilaxy_emergency"
        private const val NOTIFICATION_ID = 1001
    }
    
    override fun onCreate() {
        super.onCreate()
        SecureLogger.d(TAG, "AfilaxyFirebaseMessagingService created")
        createNotificationChannel()
    }
    
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        SecurityInterceptor.secureOperation("fcm_message_received") {
            SecureLogger.d(TAG, "Notification received")
            
            // Validate sender with centralized validation
            val fromSender = remoteMessage.from?.let { sender ->
                val validationResult = CentralizedValidator.validateInput(sender, CentralizedValidator.InputType.GENERAL)
                if (validationResult.isValid) sender else "unknown_sender"
            } ?: "unknown_sender"
            
            // Validate notification type
            val notificationType = remoteMessage.data["type"]?.let { type ->
                val validationResult = CentralizedValidator.validateInput(type, CentralizedValidator.InputType.GENERAL)
                if (validationResult.isValid) type else null
            }
            
            when (notificationType) {
                "emergency_alert" -> {
                    SecurityMonitor.reportSecurityEvent("EMERGENCY_NOTIFICATION", "Processing emergency")
                    handleEmergencyNotification(remoteMessage)
                }
                else -> {
                    handleGeneralNotification(remoteMessage)
                }
            }
        } ?: SecurityMonitor.reportSecurityEvent("FCM_VIOLATION", "Unauthorized message processing")
    }
    
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        
        try {
            // Validate token format
            if (token.isBlank() || token.length > 200 || !token.matches(Regex("^[a-zA-Z0-9_:-]+$"))) {
                SecureLogger.w(TAG, "Invalid FCM token format received")
                return
            }
            
            SecureLogger.d(TAG, "New FCM token received (${token.length} chars)")
            
            // Token will be updated in Firestore automatically by FCMTokenManager when app opens
            
        } catch (e: Exception) {
            val error = ErrorHandler.handleException(e, "NEW_FCM_TOKEN")
            SecureLogger.e(TAG, "Error processing new FCM token", e)
        }
    }
    
    private fun handleEmergencyNotification(remoteMessage: RemoteMessage) {
        try {
            // Validate and sanitize all notification data
            val rawTitle = remoteMessage.data["title"]
            val title = if (rawTitle != null && SqlInjectionPrevention.isValidSqlInput(rawTitle)) {
                InputSanitizer.sanitizeText(rawTitle).takeIf { it.isNotBlank() } ?: "🚨 EMERGÊNCIA AFILAXY"
            } else "🚨 EMERGÊNCIA AFILAXY"
            
            val rawBody = remoteMessage.data["body"]
            val body = if (rawBody != null && SqlInjectionPrevention.isValidSqlInput(rawBody)) {
                InputSanitizer.sanitizeText(rawBody).takeIf { it.isNotBlank() } ?: "Alguém precisa de ajuda próximo a você!"
            } else "Alguém precisa de ajuda próximo a você!"
            
            val rawEmergencyId = remoteMessage.data["emergencyId"]
            val emergencyId = if (rawEmergencyId != null && SqlInjectionPrevention.isValidSqlInput(rawEmergencyId)) {
                InputSanitizer.sanitizeText(rawEmergencyId)
            } else ""
            
            val rawRequesterName = remoteMessage.data["requesterName"]
            val requesterName = if (rawRequesterName != null && SqlInjectionPrevention.isValidSqlInput(rawRequesterName)) {
                InputSanitizer.sanitizeName(rawRequesterName).takeIf { it.isNotBlank() } ?: "Pessoa"
            } else "Pessoa"
            
            SecureLogger.d(TAG, "Emergency notification processed successfully")
            SecureLogger.d(TAG, "Emergency notification data validated")
        
            // Create intent to open app on emergency screen
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
            
            // Create high priority notification
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
            
            SecureLogger.d(TAG, "Emergency notification displayed successfully")
            
        } catch (e: Exception) {
            val error = ErrorHandler.handleException(e, "HANDLE_EMERGENCY_NOTIFICATION")
            SecureLogger.e(TAG, "Error handling emergency notification", e)
        }
    }
    
    private fun handleGeneralNotification(remoteMessage: RemoteMessage) {
        try {
            val rawTitle = remoteMessage.notification?.title
            val title = if (rawTitle != null && SqlInjectionPrevention.isValidSqlInput(rawTitle)) {
                InputSanitizer.sanitizeText(rawTitle).takeIf { it.isNotBlank() }
            } else null ?: "Afilaxy"
            
            val rawBody = remoteMessage.notification?.body
            val body = if (rawBody != null && SqlInjectionPrevention.isValidSqlInput(rawBody)) {
                InputSanitizer.sanitizeText(rawBody).takeIf { it.isNotBlank() }
            } else null ?: "Nova notificação"
        
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
            
            SecureLogger.d(TAG, "General notification displayed successfully")
            
        } catch (e: Exception) {
            val error = ErrorHandler.handleException(e, "HANDLE_GENERAL_NOTIFICATION")
            SecureLogger.e(TAG, "Error handling general notification", e)
        }
    }
    
    private fun createNotificationChannel() {
        try {
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
                
                SecureLogger.d(TAG, "Notification channel created: $EMERGENCY_CHANNEL_ID")
            }
        } catch (e: Exception) {
            val error = ErrorHandler.handleException(e, "CREATE_NOTIFICATION_CHANNEL")
            SecureLogger.e(TAG, "Error creating notification channel", e)
        }
    }
}