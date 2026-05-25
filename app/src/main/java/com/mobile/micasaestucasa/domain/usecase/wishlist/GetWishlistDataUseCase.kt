package com.mobile.micasaestucasa.domain.usecase.wishlist

import com.mobile.micasaestucasa.domain.model.property.Property
import com.mobile.micasaestucasa.domain.repository.user.UserRepo
import com.mobile.micasaestucasa.domain.repository.whishlist.WhishlistRepo
import com.mobile.micasaestucasa.ui.viewmodels.wishlist.Collection
import javax.inject.Inject

class GetWishlistDataUseCase @Inject constructor(
    private val wishlistRepo: WhishlistRepo,
    private val userRepo: UserRepo
) {
    suspend operator fun invoke(): Result<Pair<List<Property>, List<Collection>>> {
        return try {
            val user = userRepo.getCurrentUser()
                ?: return Result.failure(Exception("User not logged in"))

            val propsResult = wishlistRepo.getSavedProperties(user.id)
            val collsResult = wishlistRepo.getCollections(user.id)

            val props = propsResult.getOrDefault(emptyList())
            val colls = collsResult.getOrDefault(emptyList())

            Result.success(Pair(props, colls))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

