package com.mobile.micasaestucasa.domain.usecase.review

import com.mobile.micasaestucasa.domain.model.review.Review
import com.mobile.micasaestucasa.domain.repository.review.ReviewRepo
import com.mobile.micasaestucasa.domain.repository.user.UserRepo
import javax.inject.Inject

class GetPropertyReviewsUseCase @Inject constructor(
    private val reviewRepo: ReviewRepo,
    private val userRepo: UserRepo
) {
    suspend operator fun invoke(propertyId: String): Result<List<Review>> {
        if (propertyId.isBlank()) {
            return Result.failure(IllegalArgumentException("PropertyId cannot be empty"))
        }
        return reviewRepo.getPropertyReviews(propertyId).map { reviews ->
            reviews.map { review ->
                val user = userRepo.getUserById(review.authorId).getOrNull()
                review.copy(
                    authorName = user?.name ?: "Guest",
                    authorProfilePicture = user?.profileImageUrl
                )
            }
        }
    }
}
