package com.mobile.micasaestucasa.ui.viewmodels.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.mobile.micasaestucasa.domain.repository.notification.NotificationRepo
import com.mobile.micasaestucasa.domain.usecase.auth.DeleteAccountUseCase
import com.mobile.micasaestucasa.domain.usecase.auth.LoginUseCase
import com.mobile.micasaestucasa.domain.usecase.auth.LogoutUseCase
import com.mobile.micasaestucasa.domain.usecase.auth.RegisterUseCase
import com.mobile.micasaestucasa.domain.usecase.auth.ResetPasswordUseCase
import com.mobile.micasaestucasa.domain.usecase.auth.SignInWithGoogleUseCase
import com.mobile.micasaestucasa.domain.usecase.notification.SaveFCMTokenUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val resetPasswordUseCase: ResetPasswordUseCase,
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
    private val saveFCMTokenUseCase: SaveFCMTokenUseCase,
    private val notificationRepo: NotificationRepo,
    private val firebaseAuth: FirebaseAuth,
    private val deleteAccountUseCase: DeleteAccountUseCase
) : ViewModel() {

    sealed class AuthUiState {
        object Idle : AuthUiState()
        object Loading : AuthUiState()
        object AccountDeleted : AuthUiState()
        object NeedsReauth : AuthUiState()
        data class Error(val message: String) : AuthUiState()
    }

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isAuthSuccessful = MutableStateFlow(false)
    val isAuthSuccessful: StateFlow<Boolean> = _isAuthSuccessful.asStateFlow()

    private val _passwordResetSent = MutableStateFlow(false)
    val passwordResetSent: StateFlow<Boolean> = _passwordResetSent.asStateFlow()

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

    /**
     * Sends a password reset email to the given email address.
     * Updates passwordResetSent on success, or errorMessage on failure.
     */
    fun resetPassword(email: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _passwordResetSent.value = false
            resetPasswordUseCase(email)
                .onSuccess {
                    Log.i("AuthViewModel", "Password reset email sent to: $email")
                    _passwordResetSent.value = true
                    _isLoading.value = false
                }
                .onFailure { exception ->
                    Log.e("AuthViewModel", "Password reset failed for: $email", exception)
                    _errorMessage.value = exception.message ?: "Errore nell'invio dell'email"
                    _isLoading.value = false
                }
        }
    }

    // helper
    fun clearPasswordResetState() {
        _passwordResetSent.value = false
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            Log.d("AuthViewModel", "Google Sign-In attempt")
            _isLoading.value = true
            _errorMessage.value = null

            val result = signInWithGoogleUseCase(idToken)
            result.fold(
                onSuccess = {
                    Log.i("AuthViewModel", "Google Sign-In successful")
                    registerFcmToken()
                    _isAuthSuccessful.value = true
                    _isLoading.value = false
                },
                onFailure = { exception ->
                    Log.e("AuthViewModel", "Google Sign-In failed", exception)
                    _errorMessage.value = exception.message ?: "Google Sign-In failed"
                    _isLoading.value = false
                }
            )
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            deleteAccountUseCase()
                .onSuccess { _uiState.value = AuthUiState.AccountDeleted }
                .onFailure { error ->
                    if (error.message?.contains("recente") == true ||
                        error.message?.contains("login") == true
                    ) {
                        _uiState.value = AuthUiState.NeedsReauth
                    } else {
                        _uiState.value = AuthUiState.Error(error.message ?: "Errore")
                    }
                }
        }
    }

    fun reauthenticateAndDelete(password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val user = firebaseAuth.currentUser ?: return@launch
                val email = user.email ?: return@launch
                // re-autentica con email + password
                val credential = EmailAuthProvider.getCredential(email, password)
                user.reauthenticate(credential).await()
                // ora elimina
                deleteAccountUseCase()
                    .onSuccess { _uiState.value = AuthUiState.AccountDeleted }
                    .onFailure { _uiState.value = AuthUiState.Error(it.message ?: "Errore") }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error("Password errata")
            }
        }
    }
}
