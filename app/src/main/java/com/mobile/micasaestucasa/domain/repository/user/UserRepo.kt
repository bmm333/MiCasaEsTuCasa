package com.mobile.micasaestucasa.domain.repository.user

import com.mobile.micasaestucasa.domain.model.user.User

interface UserRepo {
    suspend fun getCurrentUser(): User?
    suspend fun updateBadge(hostId: String, badge: com.mobile.micasaestucasa.domain.model.user.UserBadge, avgRating: Double, reviewsCount: Int): Result<Unit>

    suspend fun updateRenterScore(
        renterId: String,
        reliabilityScore: Double,
        renterReviewsCount: Int
    ): Result<Unit>

    suspend fun updateUserProfile(user: User): Result<Unit>
    suspend fun getUserProfile(uid: String): Result<User>
    suspend fun getUserById(uid: String): Result<User?>
}
