package com.mobile.micasaestucasa.domain.usecase.admin

import com.mobile.micasaestucasa.domain.model.user.UserRole
import com.mobile.micasaestucasa.domain.repository.admin.AdminRepo
import com.mobile.micasaestucasa.domain.repository.user.UserRepo
import javax.inject.Inject

class ReactivateUserUseCase @Inject constructor(
    private val adminRepo: AdminRepo,
    private val userRepo: UserRepo
) {
    suspend operator fun invoke(targetUserId: String, adminId: String): Result<Unit> {
        if (targetUserId.isBlank() || adminId.isBlank()) {
            return Result.failure(IllegalArgumentException("IDs are required"))
        }
        val admin = userRepo.getCurrentUser()
        if (admin == null || !admin.roles.contains(UserRole.ADMIN)) {
            return Result.failure(SecurityException("Access denied"))
        }
        return adminRepo.reactivateUser(targetUserId, adminId)
    }
}
