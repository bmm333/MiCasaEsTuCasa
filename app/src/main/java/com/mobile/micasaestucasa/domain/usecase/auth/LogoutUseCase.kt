package com.mobile.micasaestucasa.domain.usecase.auth

import com.mobile.micasaestucasa.domain.repository.auth.AuthRepo
import javax.inject.Inject

class LogoutUseCase @Inject constructor(private val authRepo: AuthRepo) {
    suspend operator fun invoke() {
        authRepo.logout()
    }
}
