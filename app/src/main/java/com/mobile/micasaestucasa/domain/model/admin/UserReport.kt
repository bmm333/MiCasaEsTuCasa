package com.mobile.micasaestucasa.domain.model.admin

/**
 * Report coming in from a user for another user
 *
 * */

data class UserReport(
    val id: String = "",
    val reporterId: String = "",
    val reportedUserId: String = "",
    val reason: String = "",
    val status: ReportStatus = ReportStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis()
)

enum class ReportStatus {
    PENDING,
    ACCEPTED,
    DISMISSED
}
