package com.mobile.micasaestucasa.domain.usecase.booking

import com.mobile.micasaestucasa.domain.repository.booking.BookingRepo
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class UpdateBookingUseCase @Inject constructor(
    private val bookingRepo: BookingRepo
) {
    suspend operator fun invoke(
        bookingId: String,
        renterId: String,
        newStartDate: String,
        newEndDate: String,
        newGuestsCount: Int,
        pricePerDay: Double
    ): Result<Unit> {
        if (newStartDate.isBlank() || newEndDate.isBlank()) {
            return Result.failure(IllegalArgumentException("Date obbligatorie"))
        }
        return try {
            val start = LocalDate.parse(newStartDate)
            val end = LocalDate.parse(newEndDate)
            if (!end.isAfter(start)) {
                return Result.failure(IllegalArgumentException("La data di fine deve essere dopo quella di inizio"))
            }
            if (newGuestsCount <= 0) {
                return Result.failure(IllegalArgumentException("Numero ospiti deve essere almeno 1"))
            }
            val nights = ChronoUnit.DAYS.between(start, end).toInt()
            val newTotalPrice = nights * pricePerDay
            bookingRepo.updateBooking(
                bookingId = bookingId,
                renterId = renterId,
                newStartDate = newStartDate,
                newEndDate = newEndDate,
                newGuestsCount = newGuestsCount,
                newTotalPrice = newTotalPrice
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
