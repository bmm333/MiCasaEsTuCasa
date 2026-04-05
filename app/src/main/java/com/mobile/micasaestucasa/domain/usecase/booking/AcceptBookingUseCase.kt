package com.mobile.micasaestucasa.domain.usecase.booking

import com.mobile.micasaestucasa.domain.repository.booking.BookingRepo
import javax.inject.Inject

class AcceptBookingUseCase @Inject constructor(
    private val bookingRepo: BookingRepo
) {
    suspend operator fun invoke(
        bookingId: String,
        hostId: String
    ): Result<Unit>
    {
        if(bookingId.isBlank())
            return Result.failure(IllegalArgumentException("bookingId obbligatorio"))
        if(hostId.isBlank())
            return Result.failure(IllegalArgumentException("hostId obbligatorio"))
        return bookingRepo.acceptBooking(bookingId,hostId)
    }
}