package com.mobile.micasaestucasa.domain.usecase.review

import com.mobile.micasaestucasa.domain.model.review.Review
import com.mobile.micasaestucasa.domain.repository.review.ReviewRepo
import javax.inject.Inject

class GetPropertyReviewsUseCase @Inject constructor(private val reviewRepo: ReviewRepo) {
    suspend operator fun invoke(propertyId: String): Result<List<Review>> {
        if (propertyId.isBlank()) {
            return Result.failure(IllegalArgumentException("PropertyId cannot be empty"))
        }
        return reviewRepo.getPropertyReviews(propertyId)
    }
}
