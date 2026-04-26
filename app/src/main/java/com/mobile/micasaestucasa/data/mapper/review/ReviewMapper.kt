package com.mobile.micasaestucasa.data.mapper.review

import com.mobile.micasaestucasa.data.dto.review.ReviewDto
import com.mobile.micasaestucasa.domain.model.review.Review

fun ReviewDto.toDomain(): Review {
    return Review(
        id = id,
        title = title,
        body = body,
        stars = stars,
        createdAt = createdAt,
        propertyId = propertyId,
        authorId = authorId
    )
}

fun Review.toDto(): ReviewDto {
    return ReviewDto(
        id = id,
        title = title,
        body = body,
        stars = stars,
        createdAt = createdAt,
        propertyId = propertyId,
        authorId = authorId
    )
}
