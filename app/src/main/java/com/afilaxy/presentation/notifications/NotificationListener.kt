package com.afilaxy.presentation.notifications

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.afilaxy.MainActivity
import com.afilaxy.security.AuthValidator
import com.afilaxy.security.InputSanitizer
import com.afilaxy.utils.ErrorHandler

class NotificationListener : ViewModel() {
    private val _hasNewEmergency = MutableStateFlow(false)
    val hasNewEmergency: StateFlow<Boolean> = _hasNewEmergency.asStateFlow()
    
    private var listenerRegistration: ListenerRegistration? = null
    // Instâncias Firebase otimizadas com lazy loading
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val auth by lazy { FirebaseAuth.getInstance() }
    
    fun startListening(context: Context) {
        ErrorHandler.safeOperation {
            val currentUser = AuthValidator.requireAuthentication()
            
            listenerRegistration = firestore
                .collection("users")
                .document(currentUser.uid)
                .collection("notifications")
                .whereEqualTo("type", "emergency_alert")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        val sanitizedError = InputSanitizer.sanitizeForLog(error.message)
                        android.util.Log.e("NotificationListener", "Erro no listener de notificações: $sanitizedError")
                        return@addSnapshotListener
                    }
                    
                    snapshot?.documentChanges?.forEach { change ->
                        if (change.type == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                            _hasNewEmergency.value = true
                            
                            val intent = Intent(context, MainActivity::class.java).apply {
                                putExtra("open_emergency", true)
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            }
                            context.startActivity(intent)
                        }
                    }
                }
        }
    }
    
    fun stopListening() {
        listenerRegistration?.remove()
        listenerRegistration = null
    }
    
    fun clearNotification() {
        _hasNewEmergency.value = false
    }
    
    override fun onCleared() {
        super.onCleared()
        stopListening()
    }
}