package com.afilaxy.presentation.profile

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    
    fun loadProfile() {
        val user = auth.currentUser ?: return
        
        _uiState.value = _uiState.value.copy(
            email = user.email ?: "",
            isLoading = true
        )
        
        viewModelScope.launch {
            try {
                firestore.collection("users")
                    .document(user.uid)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            val data = document.data
                            _uiState.value = _uiState.value.copy(
                                name = data?.get("name") as? String ?: "",
                                phone = data?.get("phone") as? String ?: "",
                                asmaType = data?.get("asmaType") as? String ?: "",
                                medications = (data?.get("medications") as? List<String>) ?: emptyList(),
                                isLoading = false
                            )
                        } else {
                            _uiState.value = _uiState.value.copy(isLoading = false)
                        }
                    }
                    .addOnFailureListener {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            message = "Erro ao carregar perfil",
                            isError = true
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = "Erro: ${e.message}",
                    isError = true
                )
            }
        }
    }
    
    fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(name = name, message = null)
    }
    
    fun updatePhone(phone: String) {
        _uiState.value = _uiState.value.copy(phone = phone, message = null)
    }
    
    fun updateAsmaType(type: String) {
        _uiState.value = _uiState.value.copy(asmaType = type, message = null)
    }
    
    fun addMedication(medication: String) {
        val currentMeds = _uiState.value.medications.toMutableList()
        if (!currentMeds.contains(medication)) {
            currentMeds.add(medication)
            _uiState.value = _uiState.value.copy(medications = currentMeds, message = null)
        }
    }
    
    fun removeMedication(medication: String) {
        val currentMeds = _uiState.value.medications.toMutableList()
        currentMeds.remove(medication)
        _uiState.value = _uiState.value.copy(medications = currentMeds, message = null)
    }
    
    fun saveProfile() {
        val user = auth.currentUser ?: return
        val state = _uiState.value
        
        _uiState.value = state.copy(isLoading = true, message = null)
        
        viewModelScope.launch {
            try {
                val profileData = mapOf(
                    "name" to state.name,
                    "phone" to state.phone,
                    "asmaType" to state.asmaType,
                    "medications" to state.medications,
                    "updatedAt" to System.currentTimeMillis()
                )
                
                firestore.collection("users")
                    .document(user.uid)
                    .set(profileData, com.google.firebase.firestore.SetOptions.merge())
                    .addOnSuccessListener {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            message = "✅ Perfil salvo com sucesso!",
                            isError = false,
                            profileSaved = true
                        )
                    }
                    .addOnFailureListener { e ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            message = "❌ Erro ao salvar: ${e.message}",
                            isError = true
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = "❌ Erro: ${e.message}",
                    isError = true
                )
            }
        }
    }
}

data class ProfileUiState(
    val email: String = "",
    val name: String = "",
    val phone: String = "",
    val asmaType: String = "",
    val medications: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val message: String? = null,
    val isError: Boolean = false,
    val profileSaved: Boolean = false
)