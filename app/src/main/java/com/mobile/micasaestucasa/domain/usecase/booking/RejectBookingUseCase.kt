package com.mobile.micasaestucasa.domain.usecase.booking

import com.mobile.micasaestucasa.domain.repository.booking.BookingRepo
import javax.inject.Inject

class RejectBookingUseCase @Inject constructor(
    private val bookingRepo: BookingRepo
) {
    suspend operator fun invoke(
        bookingId: String,
        hostId: String
    ): Result<Unit> {
        if (bookingId.isBlank()) {
            return Result.failure(IllegalArgumentException("bookingId is required"))
        }
        if (hostId.isBlank()) {
            return Result.failure(IllegalArgumentException("hostId is required"))
        }
        return bookingRepo.rejectBooking(bookingId, hostId)
    }
}
