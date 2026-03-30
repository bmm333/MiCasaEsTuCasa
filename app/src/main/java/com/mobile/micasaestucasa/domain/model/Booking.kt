package com.mobile.micasaestucasa.domain.model

import java.util.Date

data class Booking(
    val id: String = "",
    val userId: String = "",
    val propertyId: String = "",
    val startDate: Date = Date(),
    val endDate: Date = Date(),
    val totalPrice: Double = 0.0,
    val status: String = "PENDING" // e.g., PENDING, CONFIRMED, CANCELLED
)
