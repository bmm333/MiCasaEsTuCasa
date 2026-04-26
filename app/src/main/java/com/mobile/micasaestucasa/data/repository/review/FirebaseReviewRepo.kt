package com.mobile.micasaestucasa.data.repository.review

import com.google.firebase.firestore.FirebaseFirestore
import com.mobile.micasaestucasa.domain.model.review.Review
import com.mobile.micasaestucasa.domain.repository.review.ReviewRepo
import javax.inject.Inject

class FirebaseReviewRepo @Inject constructor(private val firestore: FirebaseFirestore): ReviewRepo {

    private val collection=firestore.collection("reviews")
    override suspend fun WriteReview(
        review: Review,
        userId: String
    ): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun getPropertyReviews(propertyId: String): Result<List<Review>> {
        TODO("Not yet implemented")
    }

    override suspend fun getUserReviews(userId: String): Result<List<Review>> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteReview(reviewId: String): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun editReview(reviewId: String): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun replyToReview(
        reviewId: String,
        reply: String
    ): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun hasUserAlreadyReviewed(
        userId: String,
        propertyId: String
    ): Result<Boolean> {
        TODO("Not yet implemented")
    }


}