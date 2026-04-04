package com.mobile.micasaestucasa.domain.model.property

data class Property(
    val id: String,
    val ownerId: String,
    val title: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val city: String,
    val pricePerDay: Double,
    val capacity: Int,
    val keywords: List<String>,
    val imageUrls: List<String>,
    val availableFrom: String,
    val availableTo: String,
    val rating: Double = 0.0,
    val reviewsCount: Int = 0
)
