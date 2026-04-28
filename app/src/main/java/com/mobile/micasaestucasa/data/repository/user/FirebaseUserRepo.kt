package com.mobile.micasaestucasa.data.repository.user

import com.google.firebase.auth.FirebaseAuth
import com.mobile.micasaestucasa.data.dto.user.UserDTO
import com.mobile.micasaestucasa.data.mapper.user.toDomain
import com.mobile.micasaestucasa.domain.model.user.User
import com.mobile.micasaestucasa.domain.repository.user.UserRepo

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseUserRepo @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : UserRepo {
    override suspend fun getCurrentUser(): User? {
        val firebaseUser = firebaseAuth.currentUser ?: return null
        return try {
            val doc = firestore.collection("users")
                .document(firebaseUser.uid)
                .get().await()
            doc.toObject(UserDTO::class.java)?.toDomain()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun updateBadge(
        hostId: String,
        badge: com.mobile.micasaestucasa.domain.model.user.UserBadge,
        avgRating: Double,
        reviewsCount: Int
    ): Result<Unit> {
        return try {
            firestore.collection("users").document(hostId).update(
                mapOf(
                    "badge" to badge.name,
                    "avgRating" to avgRating,
                    "reviewsCount" to reviewsCount
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateRenterScore(
        renterId: String,
        reliabilityScore: Double,
        renterReviewsCount: Int
    ): Result<Unit> {
        return try {
            firestore.collection("users").document(renterId).update(
                mapOf(
                    "reliabilityScore"   to reliabilityScore,
                    "renterReviewsCount" to renterReviewsCount
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
