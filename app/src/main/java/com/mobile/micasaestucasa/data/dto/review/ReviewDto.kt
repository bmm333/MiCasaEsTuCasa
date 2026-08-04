package com.mobile.micasaestucasa.data.dto.review

data class ReviewDto(
    val id: String? = null,
    val bookingId: String? = null,
    val reviewType: String? = null,
    val propertyId: String? = null,
    val authorId: String? = null,
    val targetId: String? = null,
    val hostId: String? = null,
    val title: String? = null,
    val body: String? = null,
    val stars: Int? = null,
    val hostStars: Int? = null,
    val hostReply: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
