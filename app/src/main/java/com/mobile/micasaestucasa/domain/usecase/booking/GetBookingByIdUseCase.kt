package com.mobile.micasaestucasa.domain.usecase.booking

import com.mobile.micasaestucasa.domain.model.booking.Booking
import com.mobile.micasaestucasa.domain.repository.booking.BookingRepo
import javax.inject.Inject

class GetBookingByIdUseCase @Inject constructor(
    private val bookingRepo: BookingRepo
) {
    suspend operator fun invoke(bookingId: String): Result<Booking> {
        if (bookingId.isBlank()) {
            return Result.failure(IllegalArgumentException("bookingId is required"))
        }
        return bookingRepo.getBookingById(bookingId)
    }
}
