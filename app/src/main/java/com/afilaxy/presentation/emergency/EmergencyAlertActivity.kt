package com.afilaxy.presentation.emergency

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.afilaxy.MainActivity
import com.afilaxy.ui.theme.AfilaxyTheme

class EmergencyAlertActivity : ComponentActivity() {
    
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    
    companion object {
        const val EXTRA_EMERGENCY_ID = "emergency_id"
        const val EXTRA_REQUESTER_NAME = "requester_name"
        const val EXTRA_DISTANCE = "distance"
        
        fun createIntent(
            context: Context,
            emergencyId: String,
            requesterName: String,
            distance: String
        ): Intent {
            return Intent(context, EmergencyAlertActivity::class.java).apply {
                putExtra(EXTRA_EMERGENCY_ID, emergencyId)
                putExtra(EXTRA_REQUESTER_NAME, requesterName)
                putExtra(EXTRA_DISTANCE, distance)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or 
                       Intent.FLAG_ACTIVITY_CLEAR_TOP or
                       Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setupFullScreenAlert()
        startAlertSounds()
        
        val emergencyId = intent.getStringExtra(EXTRA_EMERGENCY_ID) ?: ""
        val requesterName = intent.getStringExtra(EXTRA_REQUESTER_NAME) ?: "Alguém"
        val distance = intent.getStringExtra(EXTRA_DISTANCE) ?: "próximo"
        
        setContent {
            AfilaxyTheme {
                EmergencyAlertScreen(
                    requesterName = requesterName,
                    distance = distance,
                    onAccept = { acceptEmergency(emergencyId) },
                    onDismiss = { dismissAlert() }
                )
            }
        }
    }
    
    private fun setupFullScreenAlert() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
        
        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            keyguardManager.requestDismissKeyguard(this, null)
        }
    }
    
    private fun startAlertSounds() {
        try {
            // Por enquanto, apenas vibração - som será adicionado depois
            android.util.Log.d("EmergencyAlert", "Emergency alert started - sound will be added later")
        } catch (e: Exception) {
            android.util.Log.w("EmergencyAlert", "Could not initialize emergency sound: ${e.javaClass.simpleName}")
        }
        
        // Vibração
        try {
            vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
            
            val pattern = longArrayOf(0, 500, 200, 500, 200, 500)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(pattern, 0)
            }
        } catch (e: Exception) {
            android.util.Log.w("EmergencyAlert", "Could not vibrate: ${e.javaClass.simpleName}")
        }
    }
    
    private fun stopAlertSounds() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        
        vibrator?.cancel()
        vibrator = null
    }
    
    private fun acceptEmergency(emergencyId: String) {
        stopAlertSounds()
        
        android.util.Log.d("EmergencyAlert", "Accepting emergency: $emergencyId")
        
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("open_emergency_response", true)
            putExtra("emergency_id", emergencyId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        
        android.util.Log.d("EmergencyAlert", "Starting MainActivity with extras")
        startActivity(intent)
        finish()
    }
    
    private fun dismissAlert() {
        stopAlertSounds()
        finish()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopAlertSounds()
    }
}

@Composable
private fun EmergencyAlertScreen(
    requesterName: String,
    distance: String,
    onAccept: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Red.copy(alpha = 0.95f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "🆘 EMERGÊNCIA DE ASMA",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Red,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    "$requesterName precisa de ajuda",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    "📍 $distance de você",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Ignorar")
                    }
                    
                    Button(
                        onClick = onAccept,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red
                        )
                    ) {
                        Text("AJUDAR", color = Color.White)
                    }
                }
            }
        }
    }
}