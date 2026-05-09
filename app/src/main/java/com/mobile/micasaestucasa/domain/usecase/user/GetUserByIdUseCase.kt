package com.mobile.micasaestucasa.domain.usecase.user

import com.mobile.micasaestucasa.domain.model.user.User
import com.mobile.micasaestucasa.domain.repository.user.UserRepo
import javax.inject.Inject

class GetUserByIdUseCase @Inject constructor(private val userRepo: UserRepo) {
    suspend operator fun invoke(userId: String): Result<User?> {
        if (userId.isBlank()) {
            return Result.failure(IllegalArgumentException("UserId cannot be empty"))
        }
        return userRepo.getUserById(userId)
    }
}
