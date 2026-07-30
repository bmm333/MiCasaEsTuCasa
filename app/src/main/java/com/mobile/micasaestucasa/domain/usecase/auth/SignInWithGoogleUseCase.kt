package com.mobile.micasaestucasa.domain.usecase.auth

import com.mobile.micasaestucasa.domain.repository.auth.AuthRepo
import javax.inject.Inject

class SignInWithGoogleUseCase @Inject constructor(private val authRepo: AuthRepo) {
    suspend operator fun invoke(idToken: String): Result<Unit> {
        return authRepo.signInWithGoogle(idToken)
    }
}
