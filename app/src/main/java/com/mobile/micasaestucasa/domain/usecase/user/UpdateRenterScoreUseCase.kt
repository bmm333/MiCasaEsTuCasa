package com.mobile.micasaestucasa.domain.usecase.user

import com.mobile.micasaestucasa.domain.model.review.ReviewType
import com.mobile.micasaestucasa.domain.repository.review.ReviewRepo
import com.mobile.micasaestucasa.domain.repository.user.UserRepo
import javax.inject.Inject

class UpdateRenterScoreUseCase @Inject constructor(
    private val userRepo: UserRepo,
    private val reviewRepo: ReviewRepo
) {

    /**
     * Recalculates and persists the renter reliability score.
     *
     * Fetches all RENTER_REVIEW reviews targeting [renterId],
     * computes the average star rating as the reliability score,
     * and updates the user profile.
     *
     * @param renterId UID of the renter whose score should be updated
     * @return [Result.success] when the score is persisted, [Result.failure] otherwise
     */
    suspend operator fun invoke(renterId: String): Result<Unit> {
        if (renterId.isBlank()) {
            return Result.failure(IllegalArgumentException("RenterId cannot be blank"))
        }
        val reviewsResult = reviewRepo.getUserReviews(renterId)
        if (reviewsResult.isFailure) {
            return Result.failure(reviewsResult.exceptionOrNull()!!)
        }
        val renterReviews = reviewsResult.getOrThrow()
            .filter { it.reviewType == ReviewType.RENTER_REVIEW }
        val count = renterReviews.size
        val reliabilityScore = if (count == 0) {
            0.0
        } else {
            renterReviews.sumOf { it.stars } / count.toDouble()
        }
        return userRepo.updateRenterScore(renterId, reliabilityScore, count)
    }
}
