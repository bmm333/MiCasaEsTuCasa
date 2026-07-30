package com.mobile.micasaestucasa.domain.usecase.auth

import com.mobile.micasaestucasa.domain.model.user.UserStatus
import com.mobile.micasaestucasa.domain.repository.auth.AuthRepo
import com.mobile.micasaestucasa.domain.repository.user.UserRepo
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepo: AuthRepo,
    private val userRepo: UserRepo
) {
    suspend operator fun invoke(email: String, password: String): Result<Unit> {
        if (email.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("Email and password cannot be empty"))
        }
        return authRepo.login(email, password).fold(
            onSuccess = {
                val user = userRepo.getCurrentUser()
                when (user?.status) {
                    UserStatus.BANNED -> {
                        authRepo.logout()
                        Result.failure(IllegalStateException("Il tuo account è stato bannato."))
                    }
                    UserStatus.SUSPENDED -> {
                        authRepo.logout()
                        Result.failure(IllegalStateException("Il tuo account è temporaneamente sospeso."))
                    }
                    else -> Result.success(Unit)
                }
            },
            onFailure = { Result.failure(it) }
        )
    }
}
