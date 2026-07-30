package com.mobile.micasaestucasa.domain.usecase.auth

import com.mobile.micasaestucasa.domain.repository.auth.AuthRepo
import javax.inject.Inject

class DeleteAccountUseCase @Inject constructor(
    private val authRepo: AuthRepo
) {
    suspend operator fun invoke(): Result<Unit> = authRepo.deleteAccount()
}
