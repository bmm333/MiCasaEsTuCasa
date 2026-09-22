package com.mobile.micasaestucasa.data.mapper.review

import com.mobile.micasaestucasa.data.dto.review.ReviewDto
import com.mobile.micasaestucasa.domain.model.review.Review
import com.mobile.micasaestucasa.domain.model.review.ReviewType

fun ReviewDto.toDomain(): Review = Review(
    id = id ?: "",
    bookingId = bookingId ?: "",
    reviewType = reviewType?.let {
        ReviewType.valueOf(it)
    } ?: ReviewType.PROPERTY_REVIEW,
    authorId = authorId ?: "",
    targetId = targetId ?: "",
    propertyId = propertyId ?: "",
    hostId = hostId ?: "",
    title = title ?: "",
    body = body ?: "",
    stars = stars ?: 0,
    hostStars = hostStars,
    hostReply = hostReply,
    createdAt = createdAt ?: 0L
)

fun Review.toDto(): ReviewDto = ReviewDto(
    id = id,
    bookingId = bookingId,
    reviewType = reviewType.name,
    authorId = authorId,
    targetId = targetId,
    propertyId = propertyId,
    hostId = hostId,
    title = title,
    body = body,
    stars = stars,
    hostStars = hostStars,
    hostReply = hostReply,
    createdAt = createdAt
)
