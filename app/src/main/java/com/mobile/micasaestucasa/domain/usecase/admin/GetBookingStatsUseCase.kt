package com.mobile.micasaestucasa.domain.usecase.admin

import com.mobile.micasaestucasa.domain.model.admin.BookingStats
import com.mobile.micasaestucasa.domain.model.user.UserRole
import com.mobile.micasaestucasa.domain.repository.admin.AdminRepo
import com.mobile.micasaestucasa.domain.repository.user.UserRepo
import javax.inject.Inject

class GetBookingStatsUseCase @Inject constructor(
    private val adminRepo: AdminRepo,
    private val userRepo: UserRepo
) {
    suspend operator fun invoke(adminId:String):Result<BookingStats>
    {
        if(adminId.isBlank())
            return Result.failure(IllegalArgumentException("AdminID not valid"))
        val admin=userRepo.getCurrentUser()
        if (admin==null||!admin.roles.contains(UserRole.ADMIN))
            return Result.failure(SecurityException("Access denied"))
        return adminRepo.getBookingStats()
    }
}