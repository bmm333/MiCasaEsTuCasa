package com.mobile.micasaestucasa.data.repository.admin

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.mobile.micasaestucasa.domain.model.admin.ActionedUser
import com.mobile.micasaestucasa.domain.model.admin.BookingStats
import com.mobile.micasaestucasa.domain.model.admin.Keyword
import com.mobile.micasaestucasa.domain.model.admin.ReportStatus
import com.mobile.micasaestucasa.domain.model.admin.UserReport
import com.mobile.micasaestucasa.domain.model.booking.BookingStatus
import com.mobile.micasaestucasa.domain.model.user.UserStatus
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

    override suspend fun updateKeyword(
        keywordId: String,
        newLabel: String,
        adminId: String
    ): Result<Unit> {
        return try {
            firestore.collection("keywords").document(keywordId)
                .update("label", newLabel).await()
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

    /**
     * Suspends a user and cascades: puts all their properties on hold
     * and cancels their active bookings (as host).
     */
    override suspend fun suspendUser(
        targetUserId: String,
        adminId: String
    ): Result<Unit> {
        return try {
            val batch = firestore.batch()

            // Update user status
            val userRef = firestore.collection("users").document(targetUserId)
            batch.set(userRef, mapOf("status" to "SUSPENDED"), SetOptions.merge())
            val propertiesSnap = firestore.collection("properties")
                .whereEqualTo("ownerId", targetUserId)
                .get().await()
            propertiesSnap.documents.forEach { doc ->
                batch.update(doc.reference, "isOnHold", true)
            }
            val bookingsSnap = firestore.collection("bookings")
                .whereEqualTo("hostId", targetUserId)
                .whereIn("status", listOf("REQUESTED", "ACCEPTED"))
                .get().await()
            bookingsSnap.documents.forEach { doc ->
                batch.update(
                    doc.reference,
                    mapOf(
                        "status" to "CANCELLED",
                        "cancellationReason" to "Host account suspended"
                    )
                )
            }
            batch.commit().await()
            Log.d("FirebaseAdminRepo", "suspendUser: suspended $targetUserId, ${propertiesSnap.size()} properties on hold, ${bookingsSnap.size()} bookings cancelled")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseAdminRepo", "suspendUser FAILED", e)
            Result.failure(e)
        }
    }

    /**
     * Permanently bans a user and cascades: puts all their properties on hold
     * and cancels their active bookings (as host).
     */
    override suspend fun banUser(
        targetUserId: String,
        adminId: String
    ): Result<Unit> {
        return try {
            val batch = firestore.batch()
            val userRef = firestore.collection("users").document(targetUserId)
            batch.set(userRef, mapOf("status" to "BANNED"), SetOptions.merge())
            val propertiesSnap = firestore.collection("properties")
                .whereEqualTo("ownerId", targetUserId)
                .get().await()
            propertiesSnap.documents.forEach { doc ->
                batch.update(doc.reference, "isOnHold", true)
            }
            val bookingsSnap = firestore.collection("bookings")
                .whereEqualTo("hostId", targetUserId)
                .whereIn("status", listOf("REQUESTED", "ACCEPTED"))
                .get().await()
            bookingsSnap.documents.forEach { doc ->
                batch.update(
                    doc.reference,
                    mapOf(
                        "status" to "CANCELLED",
                        "cancellationReason" to "Host account banned"
                    )
                )
            }
            batch.commit().await()
            Log.d("FirebaseAdminRepo", "banUser: banned $targetUserId, ${propertiesSnap.size()} properties on hold, ${bookingsSnap.size()} bookings cancelled")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseAdminRepo", "banUser FAILED", e)
            Result.failure(e)
        }
    }

    /**
     * Reactivates a suspended user and reverses the cascade:
     * removes hold from all their properties.
     */
    override suspend fun reactivateUser(
        targetUserId: String,
        adminId: String
    ): Result<Unit> {
        return try {
            val batch = firestore.batch()
            val userRef = firestore.collection("users").document(targetUserId)
            batch.set(userRef, mapOf("status" to "ACTIVE"), SetOptions.merge())
            val propertiesSnap = firestore.collection("properties")
                .whereEqualTo("ownerId", targetUserId)
                .get().await()
            propertiesSnap.documents.forEach { doc ->
                batch.update(doc.reference, "isOnHold", false)
            }
            batch.commit().await()
            Log.d("FirebaseAdminRepo", "reactivateUser: reactivated $targetUserId, ${propertiesSnap.size()} properties restored")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseAdminRepo", "reactivateUser FAILED", e)
            Result.failure(e)
        }
    }

    /**
     * Fetches all users with SUSPENDED or BANNED status for the admin panel.
     */
    override suspend fun getActionedUsers(): Result<List<ActionedUser>> {
        return try {
            val suspendedSnap = firestore.collection("users")
                .whereEqualTo("status", "SUSPENDED")
                .get().await()
            val bannedSnap = firestore.collection("users")
                .whereEqualTo("status", "BANNED")
                .get().await()

            val allDocs = suspendedSnap.documents + bannedSnap.documents
            val users = allDocs.mapNotNull { doc ->
                val id = doc.getString("id") ?: doc.id
                val name = doc.getString("name") ?: ""
                val email = doc.getString("email") ?: ""
                val statusStr = doc.getString("status") ?: return@mapNotNull null
                val status = try {
                    UserStatus.valueOf(statusStr)
                } catch (_: Exception) {
                    return@mapNotNull null
                }

                val propsOnHold = try {
                    firestore.collection("properties")
                        .whereEqualTo("ownerId", id)
                        .whereEqualTo("isOnHold", true)
                        .get().await().size()
                } catch (_: Exception) {
                    0
                }

                ActionedUser(
                    id = id,
                    name = name,
                    email = email,
                    status = status,
                    propertiesOnHold = propsOnHold
                )
            }

            Log.d("FirebaseAdminRepo", "getActionedUsers: found ${users.size} actioned users")
            Result.success(users)
        } catch (e: Exception) {
            Log.e("FirebaseAdminRepo", "getActionedUsers FAILED", e)
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
            Log.d("FirebaseAdminRepo", "getAllReports: found ${reports.size} reports")
            Result.success(reports)
        } catch (e: Exception) {
            Log.e("FirebaseAdminRepo", "getAllReports FAILED", e)
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
            Log.d("FirebaseAdminRepo", "getPendingReports: found ${reports.size} reports")
            Result.success(reports)
        } catch (e: Exception) {
            Log.e("FirebaseAdminRepo", "getPendingReports FAILED", e)
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
