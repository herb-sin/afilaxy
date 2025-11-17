package com.afilaxy.presentation.emergency

import android.content.Intent
import android.os.Bundle
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

import com.afilaxy.ui.theme.AfilaxyTheme
import com.afilaxy.data.EmergencyManager
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope

class EmergencyOverlayActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Configurar para aparecer sobre outros apps
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )
        
        val emergencyId = intent.getStringExtra("emergency_id") ?: ""
        val requesterName = intent.getStringExtra("requesterName") ?: "Alguém"
        val distance = intent.getStringExtra("distance") ?: "0"
        
        android.util.Log.d("EmergencyOverlay", "🔴 OVERLAY CRIADA! emergencyId=$emergencyId, requesterName=$requesterName, distance=$distance")
        
        setContent {
            AfilaxyTheme {
                EmergencyOverlayScreen(
                    emergencyId = emergencyId,
                    requesterName = requesterName,
                    distance = distance,
                    onAccept = { 
                        android.util.Log.d("EmergencyOverlay", "Usuário aceitou emergência: $emergencyId")
                        lifecycleScope.launch {
                            EmergencyManager.acceptEmergency(emergencyId)
                        }
                        finish()
                        startMainActivity(emergencyId, true)
                    },
                    onDecline = { 
                        android.util.Log.d("EmergencyOverlay", "Usuário recusou emergência: $emergencyId")
                        finish() 
                    }
                )
            }
        }
    }
    
    private fun startMainActivity(emergencyId: String, isResponse: Boolean) {
        val requesterName = intent.getStringExtra("requesterName") ?: "Helper"
        android.util.Log.d("EmergencyOverlay", "Navegando para emergência: $emergencyId, response: $isResponse, requester: $requesterName")
        val mainIntent = Intent(this, com.afilaxy.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("emergency_id", emergencyId)
            putExtra("open_emergency_response", isResponse)
            putExtra("requester_name", requesterName)
        }
        android.util.Log.d("EmergencyOverlay", "Intent extras: emergency_id=$emergencyId, open_emergency_response=$isResponse, requester_name=$requesterName")
        startActivity(mainIntent)
    }
}

@Composable
fun EmergencyOverlayScreen(
    emergencyId: String,
    requesterName: String,
    distance: String,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Red.copy(alpha = 0.95f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🆘 EMERGÊNCIA",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Red,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "$requesterName precisa de ajuda",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = "Distância: ${distance}m",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = onDecline,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                    ) {
                        Text("Recusar", color = Color.White)
                    }
                    
                    Button(
                        onClick = onAccept,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text("Aceitar Ajudar", color = Color.White)
                    }
                }
            }
        }
    }
}