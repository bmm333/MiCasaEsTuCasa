package com.mobile.micasaestucasa.domain.model.user

data class User(
    val id: String,
    val name: String,
    val email: String,
    val roles: List<UserRole>,
    val lastName: String = "",
    val profileCompleted: Boolean = false,
    val badge: UserBadge = UserBadge.NEW_RENTER,
    val avgRating: Double = 0.0,
    val reviewsCount: Int = 0,
    val reliabilityScore: Double = 0.0,
    val renterReviewsCount: Int = 0,
    val fcmToken: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val bio: String = "",
    val profileImageUrl: String? = null,
    val address: String = "",
    val phone: String = "",
    val status: UserStatus = UserStatus.ACTIVE,
    val isOnline: Boolean = false,
    val lastSeen: Long? = null
)
enum class UserStatus {
    ACTIVE,
    SUSPENDED,
    BANNED
}
