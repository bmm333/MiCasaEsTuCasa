package com.mobile.micasaestucasa.data.dto.user

data class UserDTO(
    val id: String? = null,
    val name: String? = null,
    val email: String? = null,
    val badge: String? = null,
    val avgRating: Double? = null,
    val reviewsCount: Int? = null,
    val reliabilityScore: Double? = null,
    val renterReviewsCount: Int? = null,
    val fcmToken: String? = null,
    val createdAt: Long? = null,
    val roles: List<String>? = null,
    val bio: String? = null,
    val profileImageUrl: String? = null,
    val address: String? = null,
    val phone: String? = null
)
