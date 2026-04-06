package com.mobile.micasaestucasa.data.mapper.property

import com.mobile.micasaestucasa.data.dto.property.PropertyDto
import com.mobile.micasaestucasa.domain.model.property.Property

fun PropertyDto.toDomain(): Property {
    return Property(
        id = id ?: "",
        ownerId = ownerId ?: "",
        title = title ?: "",
        description = description ?: "",
        latitude = latitude ?: 0.0,
        longitude = longitude ?: 0.0,
        city = city ?: "",
        pricePerDay = pricePerDay ?: 0.0,
        capacity = capacity ?: 1,
        keywords = keywords ?: emptyList(),
        imageUrls = imageUrls ?: emptyList(),
        availableFrom = availableFrom ?: "",
        availableTo = availableTo ?: "",
        rating = rating ?: 0.0,
        reviewsCount = reviewsCount ?: 0
    )
}

fun Property.toDto(): PropertyDto {
    return PropertyDto(
        id = id,
        ownerId = ownerId,
        title = title,
        description = description,
        latitude = latitude,
        longitude = longitude,
        city = city,
        pricePerDay = pricePerDay,
        capacity = capacity,
        keywords = keywords,
        imageUrls = imageUrls,
        availableFrom = availableFrom,
        availableTo = availableTo,
        rating = rating,
        reviewsCount = reviewsCount
    )
}
