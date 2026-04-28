package com.mobile.micasaestucasa.domain.usecase.review

import com.mobile.micasaestucasa.domain.repository.review.ReviewRepo
import javax.inject.Inject

class DeleteReviewUseCase @Inject constructor(private val reviewRepo: ReviewRepo) {
    suspend operator fun invoke(reviewId: String, userId: String): Result<Unit> {
        if(userId.isBlank())
            return Result.failure(IllegalArgumentException("Id utente non puo essere vuoto"))
        if(reviewId.isBlank())
            return Result.failure(IllegalArgumentException("Id recensione non puo essere vuoto"))
            
        return reviewRepo.deleteReview(reviewId, userId)
    }
}