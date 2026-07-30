package com.mobile.micasaestucasa.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.mobile.micasaestucasa.domain.model.user.UserStatus
import com.mobile.micasaestucasa.domain.repository.user.UserRepo
import com.mobile.micasaestucasa.domain.session.SessionManager
import com.mobile.micasaestucasa.domain.usecase.notification.SaveFCMTokenUseCase
import com.mobile.micasaestucasa.ui.navigation.Route
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val userRepo: UserRepo,
    private val sessionManager: SessionManager,
    private val saveFCMTokenUseCase: SaveFCMTokenUseCase,
    private val notificationRepo: com.mobile.micasaestucasa.domain.repository.notification.NotificationRepo
) : ViewModel() {

    sealed class SessionEvent {
        data object Active : SessionEvent()
        data object Banned : SessionEvent()
        data object Suspended : SessionEvent()
        data object LoggedOut : SessionEvent()
    }

    private val _startDestination = MutableStateFlow<Route?>(null)
    val startDestination: StateFlow<Route?> = _startDestination.asStateFlow()

    private val _sessionEvent = MutableStateFlow<SessionEvent>(SessionEvent.Active)
    val sessionEvent: StateFlow<SessionEvent> = _sessionEvent.asStateFlow()

    init {
        viewModelScope.launch {
            _startDestination.value = resolveStartDestination()
            registerFcmIfLoggedIn()
        }
        viewModelScope.launch {
            sessionManager.sessionStatus.collect { status ->
                when (status) {
                    UserStatus.BANNED -> {
                        auth.signOut()
                        _sessionEvent.value = SessionEvent.Banned
                    }
                    UserStatus.SUSPENDED -> {
                        auth.signOut()
                        _sessionEvent.value = SessionEvent.Suspended
                    }
                    UserStatus.ACTIVE -> {
                        _sessionEvent.value = SessionEvent.Active
                    }
                    null -> {
                        _sessionEvent.value = SessionEvent.LoggedOut
                    }
                }
            }
        }
    }

    private suspend fun registerFcmIfLoggedIn() {
        val userId = auth.currentUser?.uid ?: return
        notificationRepo.getCurrentToken()
            .onSuccess { token -> saveFCMTokenUseCase(userId, token) }
    }

    private suspend fun resolveStartDestination(): Route {
        if (auth.currentUser == null) return Route.Login
        val user = try {
            userRepo.getCurrentUser()
        } catch (_: Exception) {
            null
        }
        if (user?.status == UserStatus.BANNED || user?.status == UserStatus.SUSPENDED) {
            auth.signOut()
            return Route.Login
        }
        return if (user != null && !user.profileCompleted) {
            Route.SignupOnboarding
        } else {
            Route.Home
        }
    }
}
