package com.mobile.micasaestucasa.ui.viewmodels.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.mobile.micasaestucasa.domain.repository.notification.NotificationRepo
import com.mobile.micasaestucasa.domain.usecase.auth.LoginUseCase
import com.mobile.micasaestucasa.domain.usecase.auth.LogoutUseCase
import com.mobile.micasaestucasa.domain.usecase.auth.RegisterUseCase
import com.mobile.micasaestucasa.domain.usecase.notification.SaveFCMTokenUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val saveFCMTokenUseCase: SaveFCMTokenUseCase,
    private val notificationRepo: NotificationRepo,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isAuthSuccessful = MutableStateFlow(false)
    val isAuthSuccessful: StateFlow<Boolean> = _isAuthSuccessful.asStateFlow()

    private suspend fun registerFcmToken() {
        val userId = firebaseAuth.currentUser?.uid ?: return
        notificationRepo.getCurrentToken()
            .onSuccess { token -> saveFCMTokenUseCase(userId, token) }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            Log.d("AuthViewModel", "Login attempt for email: $email")
            _isLoading.value = true
            _errorMessage.value = null
            val result = loginUseCase(email, password)
            result.fold(
                onSuccess = {
                    Log.i("AuthViewModel", "Login successful for email: $email")
                    registerFcmToken()
                    _isAuthSuccessful.value = true
                    _isLoading.value = false
                },
                onFailure = { exception ->
                    Log.e("AuthViewModel", "Login failed for email: $email", exception)
                    _errorMessage.value = exception.message ?: "Errore di login sconosciuto"
                    _isLoading.value = false
                }
            )
        }
    }

    fun register(email: String, password: String) {
        viewModelScope.launch {
            Log.d("AuthViewModel", "Registration attempt for email: $email")
            _isLoading.value = true
            _errorMessage.value = null

            val result = registerUseCase(email, password)
            result.fold(
                onSuccess = {
                    Log.i("AuthViewModel", "Registration successful for email: $email")
                    registerFcmToken()
                    _isAuthSuccessful.value = true
                    _isLoading.value = false
                },
                onFailure = { exception ->
                    Log.e("AuthViewModel", "Registration failed for email: $email", exception)
                    _errorMessage.value = exception.message ?: "Errore di registrazione sconosciuto"
                    _isLoading.value = false
                }
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            Log.d("AuthViewModel", "Logout initiated")
            logoutUseCase()
            _isAuthSuccessful.value = false
            Log.i("AuthViewModel", "Logout successful")
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
