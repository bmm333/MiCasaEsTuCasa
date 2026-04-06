package com.mobile.micasaestucasa.data.mapper.booking

import com.mobile.micasaestucasa.data.dto.booking.BookingDto
import com.mobile.micasaestucasa.domain.model.booking.Booking
import com.mobile.micasaestucasa.domain.model.booking.BookingStatus

fun BookingDto.toDomain(): Booking {
    return Booking(
        id = id ?: "",
        propertyId = propertyId ?: "",
        renterId = renterId ?: "",
        hostId = hostId ?: "",
        startDate = startDate ?: "",
        endDate = endDate ?: "",
        guestsCount = guestsCount ?: 1,
        pricePerDay = pricePerDay ?: 0.0,
        totalPrice = totalPrice ?: 0.0,
        status = status?.let {
            BookingStatus.valueOf(it)
        } ?: BookingStatus.REQUESTED,
        idempotencyKey = idempotencyKey ?: "",
        createdAt = createdAt ?: 0L
    )
}

fun Booking.toDto(): BookingDto {
    return BookingDto(
        id = id,
        propertyId = propertyId,
        renterId = renterId,
        hostId = hostId,
        startDate = startDate,
        endDate = endDate,
        guestsCount = guestsCount,
        pricePerDay = pricePerDay,
        totalPrice = totalPrice,
        status = status.name,
        idempotencyKey = idempotencyKey,
        createdAt = createdAt
    )
}
