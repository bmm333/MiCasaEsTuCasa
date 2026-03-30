package com.mobile.micasaestucasa.domain.model

data class Property(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val pricePerNight: Double = 0.0,
    val ownerId: String = "",
    val address: String = "",
    val imageUrls: List<String> = emptyList()
)
