package com.afilaxy.presentation.emergency

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EmergencyRequestState(
    val status: String = "pending",
    val helperName: String? = null,
    val isAccepted: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class EmergencyRequestViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {
    
    private val _state = MutableStateFlow(EmergencyRequestState())
    val state: StateFlow<EmergencyRequestState> = _state.asStateFlow()
    
    fun monitorEmergencyStatus(emergencyId: String) {
        viewModelScope.launch {
            try {
                firestore.collection("emergency_requests")
                    .document(emergencyId)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            android.util.Log.e("EmergencyRequestVM", "Error monitoring status: ${error.javaClass.simpleName}")
                            _state.value = _state.value.copy(error = "Erro ao monitorar status")
                            return@addSnapshotListener
                        }
                        
                        if (snapshot != null && snapshot.exists()) {
                            val status = snapshot.getString("status") ?: "pending"
                            val helperName = snapshot.getString("helperName")
                            
                            android.util.Log.d("EmergencyRequestVM", "Status updated: $status")
                            
                            _state.value = _state.value.copy(
                                status = status,
                                helperName = helperName,
                                isAccepted = status == "accepted",
                                error = null
                            )
                        }
                    }
            } catch (e: Exception) {
                android.util.Log.e("EmergencyRequestVM", "Error setting up listener: ${e.javaClass.simpleName}")
                _state.value = _state.value.copy(error = "Erro ao conectar")
            }
        }
    }
}