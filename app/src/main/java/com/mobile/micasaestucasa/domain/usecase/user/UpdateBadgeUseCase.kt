package com.mobile.micasaestucasa.domain.usecase.user

import com.mobile.micasaestucasa.domain.model.user.UserBadge
import com.mobile.micasaestucasa.domain.repository.review.ReviewRepo
import com.mobile.micasaestucasa.domain.repository.user.UserRepo
import javax.inject.Inject

class UpdateBadgeUseCase @Inject constructor(private val userRepo: UserRepo, private val reviewRepo: ReviewRepo) {

    suspend operator fun invoke(hostId: String): Result<Unit> {
        if (hostId.isBlank()) {
            return Result.failure(IllegalArgumentException("HostId cannot be blank"))
        }
        val reviewsResult = reviewRepo.getUserReviews(hostId)
        if (reviewsResult.isFailure) {
            return Result.failure(reviewsResult.exceptionOrNull()!!)
        }
        val reviews = reviewsResult.getOrThrow()
        val count = reviews.size
        val avgrating = if (count == 0) 0.0 else reviews.sumOf { it.hostStars ?: it.stars } / count.toDouble()
        val badge = when {
            count >= 10 && avgrating >= 4.7 -> UserBadge.SUPER_HOST
            count >= 3 && avgrating >= 4.0 -> UserBadge.TRUSTED_HOST
            else -> UserBadge.NEW_HOST
        }
        return userRepo.updateBadge(hostId, badge, avgrating, count)
    }
}
