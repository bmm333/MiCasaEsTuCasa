package com.mobile.micasaestucasa.domain.usecase.review

import com.mobile.micasaestucasa.domain.model.review.Review
import com.mobile.micasaestucasa.domain.repository.review.ReviewRepo
import javax.inject.Inject

class WriteReviewUseCase @Inject constructor(private val reviewRepo: ReviewRepo) {
    suspend operator fun invoke(review: Review,userId: String): Result<Unit> {
        if(userId.isEmpty())
            return Result.failure(IllegalArgumentException("Id utente non puo essere vuoto"))
        if(review.title.isBlank())
            return Result.failure(IllegalArgumentException("Titolo obbligatorio"))
        if(review.body.isBlank())
            return Result.failure(IllegalArgumentException("Descrizione obbligatoria"))
        if (review.stars < 1 || review.stars > 5)
            return Result.failure(IllegalArgumentException("Valutazione obbligatoria compresa tra 1 e 5"))
        return reviewRepo.writeReview(review,userId)
    }
}