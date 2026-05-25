package com.mobile.micasaestucasa.domain.usecase.review

import com.mobile.micasaestucasa.domain.model.review.Review
import com.mobile.micasaestucasa.domain.repository.review.ReviewRepo
import javax.inject.Inject

class GetRenterReviewsUseCase @Inject constructor(
    private val reviewRepo: ReviewRepo
) {
    suspend operator fun invoke(renterId: String): Result<List<Review>> {
        if (renterId.isBlank()) {
            return Result.failure(IllegalArgumentException("renterId is required"))
        }
        return reviewRepo.getRenterReviews(renterId)
    }
}
