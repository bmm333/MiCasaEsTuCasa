package com.mobile.micasaestucasa.domain.usecase.review

import com.mobile.micasaestucasa.domain.model.review.ReviewType
import com.mobile.micasaestucasa.domain.repository.review.ReviewRepo
import javax.inject.Inject

class HasUserAlreadyReviewedBookingUseCase @Inject constructor(
    private val reviewRepo: ReviewRepo
) {
    suspend operator fun invoke(
        userId: String,
        bookingId: String,
        type: ReviewType
    ): Result<Boolean> {
        if (userId.isBlank()) {
            return Result.failure(IllegalArgumentException("userId is required"))
        }
        if (bookingId.isBlank()) {
            return Result.failure(IllegalArgumentException("bookingId is required"))
        }
        return reviewRepo.hasUserAlreadyReviewedBooking(userId, bookingId, type)
    }
}
