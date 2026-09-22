package com.mobile.micasaestucasa.domain.usecase.user

import com.mobile.micasaestucasa.domain.model.user.User
import com.mobile.micasaestucasa.domain.repository.user.UserRepo
import javax.inject.Inject

class UpdateUserProfileUseCase @Inject constructor(
    private val userRepo: UserRepo
) {
    suspend operator fun invoke(user: User): Result<Unit> {
        if (user.id.isBlank()) {
            return Result.failure(IllegalArgumentException("userId is required"))
        }
        if (user.name.isBlank()) {
            return Result.failure(IllegalArgumentException("name is required"))
        }
        if (user.email.isBlank()) {
            return Result.failure(IllegalArgumentException("email is required"))
        }
        return userRepo.updateUserProfile(user)
    }
}
