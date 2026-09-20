package com.mobile.micasaestucasa.domain.repository.booking

import com.mobile.micasaestucasa.domain.model.booking.Booking

interface BookingRepo {
    // idempotencyKey previene booking duplicati su retry
    suspend fun createBooking(
        booking: Booking,
        idempotencyKey: String
    ): Result<String>
    suspend fun acceptBooking(bookingId: String, hostId: String): Result<Unit>
    suspend fun rejectBooking(bookingId: String, hostId: String): Result<Unit>
    suspend fun cancelBooking(bookingId: String, userId: String): Result<Unit>
    suspend fun getBookingsForRenter(renterId: String): Result<List<Booking>>
    suspend fun getBookingsForHost(hostId: String): Result<List<Booking>>
    suspend fun getBookingById(bookingId: String): Result<Booking>

    // check per overlape date - usato dentro transaction
    suspend fun hasOverlappingBooking(
        propertyId: String,
        startDate: String,
        endDate: String
    ): Result<Boolean>

    // fetch active bookings for a property (for calendar availability)
    suspend fun getBookingsForProperty(propertyId: String): Result<List<Booking>>
}
