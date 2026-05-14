package com.mobile.micasaestucasa.domain.usecase.booking

import com.mobile.micasaestucasa.domain.model.booking.Booking
import com.mobile.micasaestucasa.domain.repository.booking.BookingRepo
import javax.inject.Inject

/**
 * Retrieves all bookings where the given user is the renter.
 * Returns them sorted by creation date (newest first, handled by repo).
 */
class GetBookingsForRenterUseCase @Inject constructor(
    private val bookingRepo: BookingRepo
) {
    suspend operator fun invoke(renterId: String): Result<List<Booking>> {
        if (renterId.isBlank()) {
            return Result.failure(IllegalArgumentException("renterId obbligatorio"))
        }
        return bookingRepo.getBookingsForRenter(renterId)
    }
}