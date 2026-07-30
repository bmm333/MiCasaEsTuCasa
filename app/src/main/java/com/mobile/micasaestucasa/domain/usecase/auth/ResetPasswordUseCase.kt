package com.mobile.micasaestucasa.domain.usecase.auth

import com.mobile.micasaestucasa.domain.repository.auth.AuthRepo
import javax.inject.Inject

/**
 * Use case that validates the email and sends a password reset email
 * through Firebase Authentication.
 */
class ResetPasswordUseCase @Inject constructor(
    private val authRepo: AuthRepo
) {
    /**
     * Validates the provided email and triggers a password reset email.
     *
     * @param email The user's email address to send the reset link to.
     * @return Result.success if the email was sent, Result.failure otherwise.
     */
    suspend operator fun invoke(email: String): Result<Unit> {
        if (email.isBlank()) {
            return Result.failure(IllegalArgumentException("Email obbligatoria"))
        }
        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        if (!email.matches(emailRegex)) {
            return Result.failure(IllegalArgumentException("Email non valida"))
        }
        return authRepo.resetPassword(email)
    }
}
