package com.mobile.micasaestucasa.domain.usecase.review

import com.mobile.micasaestucasa.domain.repository.review.ReviewRepo
import javax.inject.Inject

class HasUserAlreadyReviewedUseCase @Inject constructor(private val reviewRepo: ReviewRepo) {
    suspend operator fun invoke(userId: String, reviewId: String): Result<Boolean> {
        if (userId.isEmpty()) {
            return Result.failure(IllegalArgumentException("Id utente non puo essere vuoto"))
        }
        if (reviewId.isEmpty()) {
            return Result.failure(IllegalArgumentException("reviewId cannot be empty"))
        }
        return reviewRepo.hasUserAlreadyReviewed(userId, reviewId)
    }
}
