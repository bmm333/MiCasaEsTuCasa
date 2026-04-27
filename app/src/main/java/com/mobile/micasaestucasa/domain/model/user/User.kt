package com.mobile.micasaestucasa.domain.model.user

data class User(
    val id: String,
    val name: String,
    val email: String,
    val roles: List<UserRole>,
    val badge: UserBadge = UserBadge.NEW_HOST,
    val avgRating: Double = 0.0,
    val reviewsCount: Int = 0,
    val reliabilityScore: Double=0.0,
    val renterReviewsCount: Int=0,
    val fcmToken:String?=null,
    val createdAt:Long= System.currentTimeMillis()
)
