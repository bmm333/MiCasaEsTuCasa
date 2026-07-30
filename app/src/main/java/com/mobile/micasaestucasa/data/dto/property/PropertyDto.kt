package com.mobile.micasaestucasa.data.dto.property

data class PropertyDto(
    val id: String? = null,
    val ownerId: String? = null,
    val title: String? = null,
    val description: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val city: String? = null,
    val pricePerDay: Double? = null,
    val capacity: Int? = null,
    val keywords: List<String>? = null,
    val imageUrls: List<String>? = null,
    val availableFrom: String? = null,
    val availableTo: String? = null,
    val rating: Double? = null,
    val reviewsCount: Int? = null,
    val isOnHold: Boolean? = null
)
