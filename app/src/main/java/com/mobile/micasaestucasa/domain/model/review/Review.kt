package com.mobile.micasaestucasa.domain.model.review

data class Review(
    val id: String = "",
    val bookingId: String = "",
    val reviewType: ReviewType = ReviewType.PROPERTY_REVIEW,
    val authorId: String = "",
    val targetId: String = "",
    val propertyId: String = "",
    val hostId: String = "",
    val title: String = "",
    val body: String = "",
    val stars: Int = 0,
    val hostStars: Int? = null,
    val hostReply: String? = null,
    val authorName: String = "",
    val authorProfilePicture: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
