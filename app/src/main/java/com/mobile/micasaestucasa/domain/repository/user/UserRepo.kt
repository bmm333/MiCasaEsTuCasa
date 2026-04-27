package com.mobile.micasaestucasa.domain.repository.user

import com.mobile.micasaestucasa.domain.model.user.User

interface UserRepo {
    suspend fun getCurrentUser(): User?
    suspend fun updateBadge(hostId: String, badge: com.mobile.micasaestucasa.domain.model.user.UserBadge, avgRating: Double, reviewsCount: Int): Result<Unit>
}
