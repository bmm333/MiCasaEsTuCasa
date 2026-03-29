package com.mobile.micasaestucasa.domain.usecase.auth

import com.mobile.micasaestucasa.domain.repository.auth.AuthRepo
import javax.inject.Inject

class RegisterUseCase @Inject constructor(private val authRepo: AuthRepo) {
    suspend operator fun invoke(email: String, password: String): Result<Unit> {
        //controllo edge gia qua
        //e regex per email
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$".toRegex()
        if(email.isBlank()||password.isBlank())
        {
            return Result.failure(IllegalArgumentException("Email e password non possono essere vuoti"))
        }
        if(!email.matches(emailRegex))
        {
            return Result.failure(IllegalArgumentException("Email non valida"))
        }
        if(password.length<6)
        {
            return Result.failure(IllegalArgumentException("Password deve essere lunga almeno 6 caratteri"))
        }
        //chaia il repo
        return authRepo.register(email, password)
    }
}