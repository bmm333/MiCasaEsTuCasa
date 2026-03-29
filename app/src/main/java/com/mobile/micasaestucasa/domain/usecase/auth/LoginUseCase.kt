package com.mobile.micasaestucasa.domain.usecase.auth

import com.mobile.micasaestucasa.domain.repository.auth.AuthRepo
import javax.inject.Inject

class LoginUseCase @Inject constructor(private val authRepo: AuthRepo){
    suspend operator fun invoke(email:String,password:String): Result<Unit>{
        //edge
        if(email.isBlank()||password.isBlank())
        {
            return Result.failure(IllegalArgumentException("Email and password cannot be empty"))
        }
        return authRepo.login(email,password)
    }
}