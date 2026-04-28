package com.mobile.micasaestucasa.domain.usecase.review

import com.mobile.micasaestucasa.domain.model.review.Review
import com.mobile.micasaestucasa.domain.repository.review.ReviewRepo
import javax.inject.Inject

class GetUserReviewsUseCase @Inject constructor(private val reviewRepo: ReviewRepo) {
    suspend operator fun invoke(userId: String): Result<List<Review>> {
        if(userId.isBlank())
            return Result.failure(IllegalArgumentException("UserID obbligatorio"))
        return reviewRepo.getUserReviews(userId)
    }
}