package com.mobile.micasaestucasa.domain.usecase.auth

import com.mobile.micasaestucasa.domain.model.user.User
import com.mobile.micasaestucasa.domain.model.user.UserRole
import com.mobile.micasaestucasa.domain.repository.auth.AuthRepo
import com.mobile.micasaestucasa.domain.repository.user.UserRepo
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val authRepo: AuthRepo,
    private val userRepo: UserRepo
) {
    suspend operator fun invoke(email: String, password: String): Result<Unit> {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$".toRegex()
        if (email.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("Email e password non possono essere vuoti"))
        }
        if (!email.matches(emailRegex)) {
            return Result.failure(IllegalArgumentException("Email non valida"))
        }
        if (password.length < 6) {
            return Result.failure(IllegalArgumentException("Password deve essere lunga almeno 6 caratteri"))
        }

        // 1. Create Auth User
        val authResult = authRepo.register(email, password)

        return authResult.fold(
            onSuccess = { uid ->
                // 2. Create Firestore User Document
                val newUser = User(
                    id = uid,
                    name = email.substringBefore("@"),
                    email = email,
                    roles = listOf(UserRole.GUEST),
                    profileCompleted = false
                )
                userRepo.updateUserProfile(newUser)
            },
            onFailure = { Result.failure(it) }
        )
    }
}
