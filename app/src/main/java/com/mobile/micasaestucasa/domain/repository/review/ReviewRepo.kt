package com.mobile.micasaestucasa.domain.repository.review

import com.mobile.micasaestucasa.domain.model.review.Review
import com.mobile.micasaestucasa.domain.model.review.ReviewType
import com.mobile.micasaestucasa.domain.model.user.User

interface ReviewRepo {

    suspend fun writeReview(review: Review, userId: String):Result<Unit>

    suspend fun getPropertyReviews(propertyId:String):Result<List<Review>>

    suspend fun getUserReviews(userId: String):Result<List<Review>>

    suspend fun deleteReview(reviewId: String, userId: String): Result<Unit>

    suspend fun editReview(review: Review, userId: String): Result<Unit>

    suspend fun replyToReview(reviewId:String,hostId:String,reply:String):Result<Unit>

    suspend fun hasUserAlreadyReviewed(userId:String,propertyId: String): Result<Boolean>

    suspend fun hasUserAlreadyReviewedBooking(userId: String, bookingId: String, type: ReviewType): Result<Boolean>

    suspend fun getRenterReviews(renterId: String): Result<List<Review>>
}
