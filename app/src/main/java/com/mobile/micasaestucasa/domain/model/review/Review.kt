package com.mobile.micasaestucasa.domain.model.review

data class Review(
    val id: String = "",
    val bookingId: String = "",
    val reviewType: ReviewType = ReviewType.PROPERTY_REVIEW,
    val authorId: String = "",
    val targetId: String = "",
    val propertyId: String = "",
    val title: String = "",
    val body: String = "",
    val stars: Int = 0,
    val hostReply: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
