package com.mobile.micasaestucasa.domain.usecase.admin

import com.mobile.micasaestucasa.domain.model.user.UserRole
import com.mobile.micasaestucasa.domain.repository.admin.AdminRepo
import com.mobile.micasaestucasa.domain.repository.user.UserRepo
import javax.inject.Inject

class BanUserUseCase @Inject constructor(
    private val adminRepo: AdminRepo,
    private val userRepo: UserRepo
) {
    suspend operator fun invoke(targetUserId: String, adminId: String): Result<Unit> {
        if (targetUserId.isBlank() || adminId.isBlank()) {
            return Result.failure(IllegalArgumentException("Id non validi"))
        }
        if (targetUserId == adminId) {
            return Result.failure(IllegalArgumentException("Cannot ban self"))
        }
        val admin = userRepo.getCurrentUser()
        if (admin == null || !admin.roles.contains(UserRole.ADMIN)) {
            return Result.failure(SecurityException("Access denied"))
        }

        // Check if target user exists if they do, verify they're not an admin
        val target = userRepo.getUserById(targetUserId).getOrNull()
        if (target != null && target.roles.contains(UserRole.ADMIN)) {
            return Result.failure(IllegalArgumentException("Cannot ban another admin"))
        }

        return adminRepo.banUser(targetUserId, adminId)
    }
}

