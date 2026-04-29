package com.mobile.micasaestucasa.domain.usecase.review

import com.mobile.micasaestucasa.domain.model.booking.BookingStatus
import com.mobile.micasaestucasa.domain.model.review.Review
import com.mobile.micasaestucasa.domain.model.review.ReviewType
import com.mobile.micasaestucasa.domain.repository.booking.BookingRepo
import com.mobile.micasaestucasa.domain.repository.review.ReviewRepo
import javax.inject.Inject

class WriteReviewUseCase @Inject constructor(private val reviewRepo: ReviewRepo,private
val bookingRepo: BookingRepo) {
    suspend operator fun invoke(review: Review,userId: String): Result<Unit> {
        if(userId.isEmpty())
            return Result.failure(IllegalArgumentException("Id utente non puo essere vuoto"))
        if(review.title.isBlank())
            return Result.failure(IllegalArgumentException("Titolo obbligatorio"))
        if(review.body.isBlank())
            return Result.failure(IllegalArgumentException("Descrizione obbligatoria"))
        if (review.stars < 1 || review.stars > 5)
            return Result.failure(IllegalArgumentException("Valutazione obbligatoria compresa tra 1 e 5"))
        val bookingResult=bookingRepo.getBookingById(review.bookingId)
        if(bookingResult.isFailure)
            return Result.failure(
                IllegalStateException("Booking not found")
            )
        val booking=bookingResult.getOrThrow()
        if(booking.status!= BookingStatus.COMPLETED)
            return Result.failure(IllegalStateException("You can review the property once the stay is over"))
        when(review.reviewType)
        {
            ReviewType.PROPERTY_REVIEW->{
            if (booking.renterId != userId)
                return Result.failure(IllegalStateException("Only the renter can leave a review in the property"))
            if (review.targetId != booking.propertyId)
                return Result.failure(IllegalStateException("Dose not match the property"))

            }
            ReviewType.RENTER_REVIEW->{
                if(booking.hostId!=userId)
                    return Result.failure(IllegalStateException("Only the host can review the renter"))
                if(review.targetId!=booking.renterId)
                    return Result.failure(IllegalStateException("Dose not match the renter"))
            }
        }
        val alreadyReviewd=reviewRepo.hasUserAlreadyReviewedBooking(userId=userId,bookingId = review.bookingId, type = review.reviewType)
        if(alreadyReviewd.getOrDefault(false))
                return Result.failure(IllegalStateException("You've already given a review ${review.reviewType.name} per questo soggiorno"))
        return reviewRepo.writeReview(review,userId)
    }
}