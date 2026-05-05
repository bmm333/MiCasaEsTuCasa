package com.mobile.micasaestucasa.domain.usecase.wishlist

import com.mobile.micasaestucasa.domain.model.property.Property
import com.mobile.micasaestucasa.ui.viewmodels.wishlist.Collection
import javax.inject.Inject

class GetWishlistDataUseCase @Inject constructor() {
    // Restituisce un Result contenente un Pair per permettere la distrutturazione (props, colls)
    suspend operator fun invoke(): Result<Pair<List<Property>, List<Collection>>> {
        return try {
            Result.success(Pair(emptyList(), emptyList()))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
