package com.mobile.micasaestucasa.domain.usecase.property

import com.mobile.micasaestucasa.domain.model.user.UserRole
import com.mobile.micasaestucasa.domain.repository.user.UserRepo
import javax.inject.Inject

class DemoteHostUseCase @Inject constructor(
    private val userRepo: UserRepo
) {
    suspend operator fun invoke(userId: String): Result<Unit> {
        val currentUser = userRepo.getCurrentUser() ?: return Result.failure(Exception("User not found"))
        if (currentUser.roles.contains(UserRole.OWNER)) {
            val updatedRoles = currentUser.roles.filter { it != UserRole.OWNER }
            return userRepo.updateUserRolesAndBadge(userId, updatedRoles, com.mobile.micasaestucasa.domain.model.user.UserBadge.NEW_RENTER)
        }
        return Result.success(Unit)
    }
}
