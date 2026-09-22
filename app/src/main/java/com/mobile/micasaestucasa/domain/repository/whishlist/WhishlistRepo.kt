package com.mobile.micasaestucasa.domain.repository.whishlist

import com.mobile.micasaestucasa.domain.model.property.Property
import com.mobile.micasaestucasa.ui.viewmodels.wishlist.Collection

interface WhishlistRepo {
    suspend fun getSavedProperties(userId: String): Result<List<Property>>
    suspend fun getCollections(userId: String): Result<List<Collection>>
    suspend fun toggleSavedProperty(userId: String, propertyId: String): Result<Boolean>
    suspend fun isPropertySaved(userId: String, propertyId: String): Result<Boolean>
    suspend fun getSavedPropertyIds(userId: String): Result<Set<String>>
}
