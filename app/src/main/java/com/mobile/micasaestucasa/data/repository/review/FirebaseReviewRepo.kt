package com.mobile.micasaestucasa.data.repository.review

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.mobile.micasaestucasa.data.dto.review.ReviewDto
import com.mobile.micasaestucasa.data.mapper.review.toDomain
import com.mobile.micasaestucasa.data.mapper.review.toDto
import com.mobile.micasaestucasa.domain.model.review.Review
import com.mobile.micasaestucasa.domain.model.review.ReviewType
import com.mobile.micasaestucasa.domain.repository.review.ReviewRepo
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseReviewRepo @Inject constructor(
    private val firestore: FirebaseFirestore
) : ReviewRepo {

    private val collection = firestore.collection("reviews")

    override suspend fun writeReview(
        review: Review,
        userId: String
    ): Result<Unit> {
        return try {
            val docRef = collection.document()
            val dto = review.copy(id = docRef.id, authorId = userId).toDto()
            docRef.set(dto).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPropertyReviews(
        propertyId: String
    ): Result<List<Review>> {
        return try{
            val snapshot=collection
                .whereEqualTo("propertyId",propertyId)
                .orderBy("createdAt",Query.Direction.DESCENDING)
                .get().await()
            Result.success(
                snapshot.documents.mapNotNull {
                    it.toObject(ReviewDto::class.java)?.toDomain()
                }
            )
        }catch (e: Exception)
        {
            Result.failure(e)
        }
    }
    override suspend fun getUserReviews(userId: String): Result<List<Review>> {
        return try {
            val snapshot=collection
                .whereEqualTo("authorId",userId)
                .orderBy("createdAt",Query.Direction.DESCENDING)
                .get().await()
            Result.success(
                snapshot.documents.mapNotNull {
                    it.toObject(ReviewDto::class.java)?.toDomain()})
        }catch(e: Exception)
        {
            Result.failure(e)
        }
    }
    override suspend fun deleteReview(
        reviewId: String,
        userId: String
    ): Result<Unit> {
        return try {
            //ownershipt check before proceeding
            val doc=collection.document(reviewId).get().await()
            if(doc.getString("authorId")!=userId)
                return Result.failure(SecurityException("User not authorized to delete this reivew1!!! [H-01]"))
            collection.document(reviewId).delete().await()
            Result.success(Unit)
        }catch (e: Exception)
        {
            Result.failure(e)
        }
    }
    override suspend fun editReview(
        review: Review,
        userId: String
    ): Result<Unit> {
        //samething as before we do an ownership check before proceeding
        return try{
            val doc=collection.document(review.id).get().await()
            if(doc.getString("authorId")!=userId)
                return Result.failure(SecurityException("User not authorized to edit this review!! [H-02]"))
            collection.document(review.id).set(review.toDto()).await()
            Result.success(Unit)
        }catch (e: Exception)
        {
            Result.failure(e)
        }
    }

    override suspend fun replyToReview(
        reviewId: String,
        hostId:String,
        reply: String
    ): Result<Unit> {
        return try {
            val doc = collection.document(reviewId).get().await()
            if (doc.getString("hostId") != hostId)
                return Result.failure(SecurityException("Non autorizzato [H-03]"))
            collection.document(reviewId)
                .update("hostReply", reply).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun hasUserAlreadyReviewed(
        userId: String,
        propertyId: String
    ): Result<Boolean> {
        return try{
            val snapshot=collection.whereEqualTo("authorId",userId).whereEqualTo("propertyId",propertyId).limit(1).get().await()
            Result.success(!snapshot.isEmpty)
        }catch (e: Exception)
        {
            Result.failure(e)
        }
    }


    override suspend fun hasUserAlreadyReviewedBooking(
        userId: String,
        bookingId: String,
        type: ReviewType
    ): Result<Boolean> {
        return try {
            val snapshot = collection
                .whereEqualTo("authorId", userId)
                .whereEqualTo("bookingId", bookingId)
                .whereEqualTo("reviewType", type.name)
                .limit(1)
                .get().await()
            Result.success(!snapshot.isEmpty)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getRenterReviews(renterId: String): Result<List<Review>> {
        return try {
            val snapshot = collection
                .whereEqualTo("targetId", renterId)
                .whereEqualTo("reviewType", ReviewType.RENTER_REVIEW.name)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get().await()
            Result.success(
                snapshot.documents.mapNotNull {
                    it.toObject(ReviewDto::class.java)?.toDomain()
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}