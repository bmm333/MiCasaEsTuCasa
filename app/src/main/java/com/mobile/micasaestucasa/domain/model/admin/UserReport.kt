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
    val description: String = "",
    /** Optional context: which property the report is about */
    val propertyId: String? = null,
    /** Optional context: which booking the report is about */
    val bookingId: String? = null,
    /** Stored as String to avoid Firestore enum deserialization issues */
    val status: String = ReportStatus.PENDING.name,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun statusEnum(): ReportStatus =
        try { ReportStatus.valueOf(status) } catch (_: Exception) { ReportStatus.PENDING }
}

enum class ReportStatus {
    PENDING,
    ACCEPTED,
    DISMISSED
}
