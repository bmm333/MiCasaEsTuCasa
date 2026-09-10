package com.mobile.micasaestucasa.domain.repository.user

import com.mobile.micasaestucasa.domain.model.user.User
import kotlinx.coroutines.flow.Flow

interface UserRepo {
    suspend fun getCurrentUser(): User?
    suspend fun updateBadge(hostId: String, badge: com.mobile.micasaestucasa.domain.model.user.UserBadge, avgRating: Double, reviewsCount: Int): Result<Unit>

    suspend fun updateRenterScore(
        renterId: String,
        reliabilityScore: Double,
        renterReviewsCount: Int,
        badge: com.mobile.micasaestucasa.domain.model.user.UserBadge
    ): Result<Unit>

    suspend fun updateUserProfile(user: User): Result<Unit>
    suspend fun getUserProfile(uid: String): Result<User>
    suspend fun getUserById(uid: String): Result<User?>
    suspend fun updateUserRolesAndBadge(userId: String, roles: List<com.mobile.micasaestucasa.domain.model.user.UserRole>, badge: com.mobile.micasaestucasa.domain.model.user.UserBadge? = null): Result<Unit>
    fun observeUserOnlineStatus(uid: String): Flow<Pair<Boolean, Long?>>
    suspend fun updatePresence(uid: String, isOnline: Boolean)
}
