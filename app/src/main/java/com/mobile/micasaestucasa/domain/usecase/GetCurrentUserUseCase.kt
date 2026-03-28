package com.mobile.micasaestucasa.domain.usecase

import com.mobile.micasaestucasa.domain.repository.UserRepository
import com.mobile.micasaestucasa.domain.model.User
import javax.inject.Inject

class GetCurrentUserUseCase @Inject constructor(    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): User?{
        return userRepository.getCurrentUser()
    }
}