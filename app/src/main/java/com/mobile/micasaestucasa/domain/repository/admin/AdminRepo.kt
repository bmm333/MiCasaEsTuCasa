package com.mobile.micasaestucasa.domain.repository.admin

import com.mobile.micasaestucasa.domain.model.admin.BookingStats
import com.mobile.micasaestucasa.domain.model.admin.Keyword
import com.mobile.micasaestucasa.domain.model.admin.ReportStatus
import com.mobile.micasaestucasa.domain.model.admin.UserReport

/**
 * Contract for admin operations
 *
 * Each operation verifies that the user calling has the admin role before proceeding
 * An non admin user will recive an Result.failure with SecurityException
 * Second line of defense comes from Firestroe Security Rules
 * */
interface AdminRepo {
    suspend fun addKeyword(label: String, adminId: String): Result<String>
    suspend fun deleteKeyword(keywordId: String, adminId: String): Result<Unit>
    suspend fun updateKeyword(keywordId: String, newLabel: String, adminId: String): Result<Unit>
    suspend fun getAllKeywords(): Result<List<Keyword>>

    /**
     * suspend temporarley an user
     * the user cannot access the app but the account is recoverable
     * */
    suspend fun suspendUser(targetUserId: String, adminId: String): Result<Unit>

    /**
     * Perma Ban a user
     * Irreversible action.
     * */
    suspend fun banUser(targetUserId: String, adminId: String): Result<Unit>

    /**Riactivate user from temp ban*/
    suspend fun reactivateUser(targetUserId: String, adminId: String): Result<Unit>

    /**Get all reports in pending status*/
    suspend fun getAllReports(): Result<List<UserReport>>

    /**Update the status of an report*/
    suspend fun resolveReport(
        reportId: String,
        status: ReportStatus,
        adminId: String
    ): Result<Unit>
    suspend fun getBookingStats(): Result<BookingStats>
    suspend fun getPendingReports(): Result<List<UserReport>>

    /** Creates a new user report document */
    suspend fun addUserReport(report: UserReport): Result<Unit>
}
