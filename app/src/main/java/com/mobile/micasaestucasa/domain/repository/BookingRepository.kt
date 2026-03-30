package com.mobile.micasaestucasa.domain.repository

import com.mobile.micasaestucasa.domain.model.Booking

interface BookingRepository {
    suspend fun getBooking(bookingId: String): Booking?
    suspend fun getBookingsByUser(userId: String): List<Booking>
    suspend fun saveBooking(booking: Booking)
    suspend fun deleteBooking(bookingId: String)
    suspend fun updateBooking(booking: Booking)
}
