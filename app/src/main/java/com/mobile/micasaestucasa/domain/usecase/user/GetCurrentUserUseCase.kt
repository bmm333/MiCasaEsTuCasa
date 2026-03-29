package com.mobile.micasaestucasa.domain.usecase.user

import com.mobile.micasaestucasa.domain.model.user.User
import com.mobile.micasaestucasa.domain.repository.user.UserRepository
import javax.inject.Inject

class GetCurrentUserUseCase @Inject constructor(private val userRepository: UserRepository
) {
    suspend operator fun invoke(): User?{
        return userRepository.getCurrentUser()
    }
}