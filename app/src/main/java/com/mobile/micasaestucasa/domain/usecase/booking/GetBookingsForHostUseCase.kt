package com.mobile.micasaestucasa.domain.usecase.booking

import com.mobile.micasaestucasa.domain.model.booking.Booking
import com.mobile.micasaestucasa.domain.repository.booking.BookingRepo
import javax.inject.Inject

/**
 * Retrieves all bookings where the given user is the host/property owner.
 * Returns them sorted by creation date (newest first, handled by repo).
 */
class GetBookingsForHostUseCase @Inject constructor(
    private val bookingRepo: BookingRepo
) {
    suspend operator fun invoke(hostId: String): Result<List<Booking>> {
        if (hostId.isBlank()) {
            return Result.failure(IllegalArgumentException("hostId obbligatorio"))
        }
        return bookingRepo.getBookingsForHost(hostId)
    }
}