package com.mobile.micasaestucasa.data.dto.booking

data class BookingDto(
    val id: String? = null,
    val propertyId: String? = null,
    val renterId: String? = null,
    val hostId: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val guestsCount: Int? = null,
    val pricePerDay: Double? = null,
    val totalPrice: Double? = null,
    val status: String? = null,
    val idempotencyKey: String? = null,
    val createdAt: Long? = null
)