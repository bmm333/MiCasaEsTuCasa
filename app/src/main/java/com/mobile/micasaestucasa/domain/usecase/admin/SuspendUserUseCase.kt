package com.mobile.micasaestucasa.domain.usecase.admin

import com.mobile.micasaestucasa.domain.model.user.UserRole
import com.mobile.micasaestucasa.domain.repository.admin.AdminRepo
import com.mobile.micasaestucasa.domain.repository.user.UserRepo
import javax.inject.Inject

class SuspendUserUseCase @Inject constructor(
    private val adminRepo: AdminRepo,
    private val userRepo: UserRepo
) {
    suspend operator fun invoke(targetUserId: String, adminId: String): Result<Unit> {
        if (targetUserId.isBlank() || adminId.isBlank()) {
            return Result.failure(IllegalArgumentException("Id non validi"))
        }
        if (targetUserId == adminId) {
            return Result.failure(IllegalArgumentException("Cannot suspend self"))
        }

        val admin = userRepo.getCurrentUser()
        if (admin == null || !admin.roles.contains(UserRole.ADMIN)) {
            return Result.failure(SecurityException("Access denied"))
        }

        val targetResult = userRepo.getUserById(targetUserId)
        if (targetResult.isSuccess) {
            val target = targetResult.getOrThrow() ?: return Result.failure(
                IllegalArgumentException("Utente non trovato")
            )
            // target is non-null here, so target.roles is non-null
            if (target.roles.contains(UserRole.ADMIN)) {
                return Result.failure(
                    IllegalArgumentException("Non puoi sospendere un altro admin")
                )
            }
        }
        return adminRepo.suspendUser(targetUserId, adminId)
    }
}
