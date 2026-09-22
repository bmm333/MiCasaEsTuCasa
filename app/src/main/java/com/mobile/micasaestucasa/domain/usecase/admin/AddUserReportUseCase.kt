package com.mobile.micasaestucasa.domain.usecase.admin

import com.mobile.micasaestucasa.domain.model.admin.UserReport
import com.mobile.micasaestucasa.domain.repository.admin.AdminRepo
import javax.inject.Inject

class AddUserReportUseCase @Inject constructor(
    private val adminRepo: AdminRepo
) {
    suspend operator fun invoke(
        reporterId: String,
        reportedUserId: String,
        reason: String,
        description: String = "",
        propertyId: String? = null,
        bookingId: String? = null
    ): Result<Unit> {
        if (reporterId.isBlank()) {
            return Result.failure(IllegalArgumentException("reporterId is required"))
        }
        if (reportedUserId.isBlank()) {
            return Result.failure(IllegalArgumentException("reportedUserId is required"))
        }
        if (reporterId == reportedUserId) {
            return Result.failure(IllegalArgumentException("Cannot report yourself"))
        }
        if (reason.isBlank()) {
            return Result.failure(IllegalArgumentException("A reason is required"))
        }

        val report = UserReport(
            reporterId = reporterId,
            reportedUserId = reportedUserId,
            reason = reason,
            description = description,
            propertyId = propertyId,
            bookingId = bookingId
        )
        return adminRepo.addUserReport(report)
    }
}
