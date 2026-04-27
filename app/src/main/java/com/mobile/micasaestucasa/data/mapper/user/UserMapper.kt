package com.mobile.micasaestucasa.data.mapper.user

import com.mobile.micasaestucasa.data.dto.user.UserDTO
import com.mobile.micasaestucasa.domain.model.user.User
import com.mobile.micasaestucasa.domain.model.user.UserBadge
import com.mobile.micasaestucasa.domain.model.user.UserRole

fun UserDTO.toDomain(): User = User(
    id                 = id ?: "",
    name               = name ?: "",
    email              = email ?: "",
    roles              = roles
        ?.mapNotNull { UserRole.fromString(it) }
        ?: listOf(UserRole.GUEST),
    badge              = badge?.let {
        try { UserBadge.valueOf(it) }
        catch (e: IllegalArgumentException) { UserBadge.NEW_HOST }
    } ?: UserBadge.NEW_HOST,
    avgRating          = avgRating ?: 0.0,
    reviewsCount       = reviewsCount ?: 0,
    reliabilityScore   = reliabilityScore ?: 0.0,
    renterReviewsCount = renterReviewsCount ?: 0,
    fcmToken           = fcmToken,
    createdAt          = createdAt ?: System.currentTimeMillis()
)

fun User.toDto(): UserDTO = UserDTO(
    id                 = id,
    name               = name,
    email              = email,
    roles              = roles.map { it.name },
    badge              = badge.name,
    avgRating          = avgRating,
    reviewsCount       = reviewsCount,
    reliabilityScore   = reliabilityScore,
    renterReviewsCount = renterReviewsCount,
    fcmToken           = fcmToken,
    createdAt          = createdAt
)