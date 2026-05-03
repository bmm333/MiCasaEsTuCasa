package com.mobile.micasaestucasa.ui.viewmodels.wishlist

import com.mobile.micasaestucasa.domain.model.property.Property

data class Collection(
    val id: String,
    val name: String,
    val propertyCount: Int,
    val coverImageUrl: String
)

data class WishlistUiState(
    val isLoading: Boolean = false,
    val properties: List<Property> = emptyList(),
    val collections: List<Collection> = emptyList(),
    val selectedTab: Int = 0, // 0: All, 1: Collections, 2: Shared
    val error: String? = null
)