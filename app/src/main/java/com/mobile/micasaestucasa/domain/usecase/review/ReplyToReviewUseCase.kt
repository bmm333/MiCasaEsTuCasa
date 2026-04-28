package com.mobile.micasaestucasa.domain.usecase.review

import com.mobile.micasaestucasa.domain.repository.review.ReviewRepo
import javax.inject.Inject

class ReplyToReviewUseCase @Inject constructor(private val reviewRepo: ReviewRepo) {
    suspend operator fun invoke(reviewId:String,hostId:String,reply:String):Result<Unit>
    {
        if(reviewId.isBlank())
            return Result.failure(IllegalArgumentException("reviewId cannot be empty"))
        if(reply.isBlank())
            return Result.failure(IllegalArgumentException("reply cannot be empty"))
        if(hostId.isBlank())
            return Result.failure(IllegalArgumentException("hostid cannot be empty"))
        return reviewRepo.replyToReview(reviewId, hostId,reply)
    }
}