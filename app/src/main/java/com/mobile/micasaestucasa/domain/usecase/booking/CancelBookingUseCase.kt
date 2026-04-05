package com.mobile.micasaestucasa.domain.usecase.booking

import com.mobile.micasaestucasa.domain.repository.booking.BookingRepo
import javax.inject.Inject

class CancelBookingUseCase @Inject constructor(private val bookingRepo: BookingRepo) {

    suspend operator fun invoke(
        bookingId:String,
        userId:String
    ): Result<Unit>
    {
        if(bookingId.isBlank())
            return Result.failure(IllegalArgumentException("bookingId obbligatorio"))
        if(userId.isBlank())
            return Result.failure(IllegalArgumentException("userId obbligatorio"))
        return bookingRepo.cancelBooking(bookingId,userId)
    }
}