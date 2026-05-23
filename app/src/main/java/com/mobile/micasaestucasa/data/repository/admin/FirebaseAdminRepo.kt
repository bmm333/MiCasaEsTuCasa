package com.mobile.micasaestucasa.data.repository.admin

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.mobile.micasaestucasa.domain.model.admin.BookingStats
import com.mobile.micasaestucasa.domain.model.admin.Keyword
import com.mobile.micasaestucasa.domain.model.admin.ReportStatus
import com.mobile.micasaestucasa.domain.model.admin.UserReport
import com.mobile.micasaestucasa.domain.model.booking.BookingStatus
import com.mobile.micasaestucasa.domain.repository.admin.AdminRepo
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * keywords/{keywordId}
 * users/{userId}
 * reports/{reportId}
 * bookings/
 * */
class FirebaseAdminRepo @Inject constructor(
    private val firestore: FirebaseFirestore
) : AdminRepo {
    override suspend fun addKeyword(
        label: String,
        adminId: String
    ): Result<String> {
        return try {
            val docRef = firestore.collection("keywords").document()
            val keyword = Keyword(id = docRef.id, label = label)
            docRef.set(keyword).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteKeyword(
        keywordId: String,
        adminId: String
    ): Result<Unit> {
        return try {
            firestore.collection("keywords").document(keywordId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAllKeywords(): Result<List<Keyword>> {
        return try {
            val snapshot = firestore.collection("keywords")
                .orderBy("label")
                .get().await()
            Result.success(
                snapshot.documents.mapNotNull {
                    it.toObject(Keyword::class.java)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun suspendUser(
        targetUserId: String,
        adminId: String
    ): Result<Unit> {
        return try {
            firestore.collection("users").document(targetUserId)
                .set(mapOf("status" to "SUSPENDED"), com.google.firebase.firestore.SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun banUser(
        targetUserId: String,
        adminId: String
    ): Result<Unit> {
        return try {
            firestore.collection("users").document(targetUserId)
                .set(mapOf("status" to "BANNED"), com.google.firebase.firestore.SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun reactivateUser(
        targetUserId: String,
        adminId: String
    ): Result<Unit> {
        return try {
            firestore.collection("users").document(targetUserId)
                .set(mapOf("status" to "ACTIVE"), com.google.firebase.firestore.SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAllReports(): Result<List<UserReport>> {
        return try {
            val snapshot = firestore.collection("reports")
                .whereEqualTo("status", ReportStatus.PENDING.name)
                .get().await()
            val reports = snapshot.documents.mapNotNull {
                it.toObject(UserReport::class.java)
            }.sortedByDescending { it.createdAt }
            android.util.Log.d("FirebaseAdminRepo", "getAllReports: found ${reports.size} reports")
            Result.success(reports)
        } catch (e: Exception) {
            android.util.Log.e("FirebaseAdminRepo", "getAllReports FAILED", e)
            Result.failure(e)
        }
    }

    override suspend fun resolveReport(
        reportId: String,
        status: ReportStatus,
        adminId: String
    ): Result<Unit> {
        return try {
            firestore.collection("reports").document(reportId)
                .update("status", status.name).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getBookingStats(): Result<BookingStats> {
        return try {
            val snapshot = firestore.collection("bookings").get().await()
            val bookings = snapshot.documents.mapNotNull {
                it.getString("status")
            }
            Result.success(
                BookingStats(
                    total = bookings.size,
                    completed = bookings.count { it == BookingStatus.COMPLETED.name },
                    active = bookings.count { it == BookingStatus.ACCEPTED.name },
                    pending = bookings.count { it == BookingStatus.REQUESTED.name },
                    cancelled = bookings.count { it == BookingStatus.CANCELLED.name },
                    rejected = bookings.count { it == BookingStatus.REJECTED.name }
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPendingReports(): Result<List<UserReport>> {
        return try {
            val snapshot = firestore.collection("reports")
                .whereEqualTo("status", ReportStatus.PENDING.name)
                .get().await()
            val reports = snapshot.documents.mapNotNull {
                it.toObject(UserReport::class.java)
            }.sortedByDescending { it.createdAt }
            android.util.Log.d("FirebaseAdminRepo", "getPendingReports: found ${reports.size} reports")
            Result.success(reports)
        } catch (e: Exception) {
            android.util.Log.e("FirebaseAdminRepo", "getPendingReports FAILED", e)
            Result.failure(e)
        }
    }

    override suspend fun addUserReport(report: UserReport): Result<Unit> {
        return try {
            val docRef = firestore.collection("reports").document()
            val reportWithId = report.copy(id = docRef.id)
            docRef.set(reportWithId).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
