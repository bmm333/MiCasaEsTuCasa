package com.mobile.micasaestucasa.domain.usecase.auth

import com.google.firebase.auth.FirebaseAuth
import com.mobile.micasaestucasa.domain.repository.auth.AuthRepo
import com.mobile.micasaestucasa.domain.usecase.notification.RemoveFCMTokenUseCase
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val authRepo: AuthRepo,
    private val removeFCMTokenUseCase: RemoveFCMTokenUseCase,
    private val firebaseAuth: FirebaseAuth
) {
    suspend operator fun invoke() {
        val userId = firebaseAuth.currentUser?.uid
        if (!userId.isNullOrBlank()) {
            removeFCMTokenUseCase(userId)
        }
        authRepo.logout()
    }
}
