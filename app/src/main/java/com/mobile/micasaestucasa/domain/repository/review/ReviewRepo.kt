package com.mobile.micasaestucasa.domain.repository.review

import com.mobile.micasaestucasa.domain.model.review.Review
import com.mobile.micasaestucasa.domain.model.user.User

interface ReviewRepo {

    suspend fun WriteReview(review: Review, userId: String):Result<Unit>

    suspend fun getPropertyReviews(propertyId:String):Result<List<Review>>

    suspend fun getUserReviews(userId: String):Result<List<Review>>

    suspend fun deleteReview(reviewId:String):Result<Unit>

    suspend fun editReview(reviewId:String):Result<Unit>

    suspend fun replyToReview(reviewId:String,reply:String):Result<Unit>

    suspend fun hasUserAlreadyReviewed(userId:String,propertyId: String): Result<Boolean>
}