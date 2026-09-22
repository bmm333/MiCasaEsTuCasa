package com.mobile.micasaestucasa.domain.model.booking

data class Booking(
    val id: String,
    val propertyId: String,
    val renterId: String,
    val hostId: String,
    val startDate: String, // in formato ISO : yyyy-mm-dd
    val endDate: String, // "                         "
    val guestsCount: Int,
    val pricePerDay: Double,
    val status: BookingStatus,
    val totalPrice: Double,
    val idempotencyKey: String, // uuid dal req del client , come layer di protezione dai retry
    val createdAt: Long = System.currentTimeMillis()
)
