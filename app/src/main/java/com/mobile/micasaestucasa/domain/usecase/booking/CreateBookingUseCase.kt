package com.mobile.micasaestucasa.domain.usecase.booking

import com.mobile.micasaestucasa.domain.model.booking.Booking
import com.mobile.micasaestucasa.domain.repository.booking.BookingRepo
import com.mobile.micasaestucasa.domain.repository.property.PropertyRepo
import java.time.LocalDate
import java.time.format.DateTimeParseException
import javax.inject.Inject

class CreateBookingUseCase @Inject constructor(
    private val bookingRepo: BookingRepo,
    private val propertyRepo: PropertyRepo
) {
    companion object {
        private const val MAX_RETRIES = 3
        private const val BASE_DELAY_MS = 500L
    }
    suspend operator fun invoke(
        booking: Booking,
        idempotencyKey: String
    ): Result<String> {
        if (booking.propertyId.isBlank()) {
            return Result.failure(IllegalArgumentException("PropertyId Obbligatorio"))
        }
        if (booking.renterId.isBlank()) {
            return Result.failure(IllegalArgumentException("RenterId obbligatorio"))
        }
        if (booking.startDate >= booking.endDate) {
            return Result.failure(IllegalArgumentException("Data inizio deve essere prima della data fine"))
        }
        if (booking.guestsCount <= 0) {
            return Result.failure(IllegalArgumentException("numero ospiti deve essere almeno 1"))
        }
        if (booking.renterId == booking.hostId) {
            return Result.failure(IllegalArgumentException("non puoi prenotare la tua stessa proprieta"))
        }

        val propertyResult = propertyRepo.getPropertyById(booking.propertyId)
        if (propertyResult.isFailure) {
            return Result.failure(
                propertyResult.exceptionOrNull()
                    ?: IllegalArgumentException("Proprieta non trovata")
            )
        }
        val property = propertyResult.getOrThrow()
        try {
            val bookingStart = LocalDate.parse(booking.startDate)
            val bookingEnd = LocalDate.parse(booking.endDate)
            val availableFrom = LocalDate.parse(property.availableFrom)
            val availableTo = LocalDate.parse(property.availableTo)
            if (bookingStart.isBefore(availableFrom) || bookingEnd.isAfter(availableTo)) {
                return Result.failure(
                    IllegalArgumentException("Le date selezionate non rientrano nel periodo di disponibilita dell'ospite")
                )
            }
        } catch (_: DateTimeParseException) {
            return Result.failure(IllegalArgumentException("Formato date non valido"))
        }

        val overlapResult = bookingRepo.hasOverlappingBooking(booking.propertyId, booking.startDate, booking.endDate)
        if (overlapResult.isFailure) {
            return Result.failure(overlapResult.exceptionOrNull()!!)
        }
        if (overlapResult.getOrDefault(false)) {
            return Result.failure(IllegalStateException("date non disponibili per questa proprieta"))
        }
        return retryWithBackoff(MAX_RETRIES, BASE_DELAY_MS) {
            bookingRepo.createBooking(booking, idempotencyKey)
        }
    }
    private suspend fun <T> retryWithBackoff(
        maxRetries: Int,
        baseDelayMs: Long,
        block: suspend () -> Result<T>
    ): Result<T> {
        var attempt = 0
        var lastResult: Result<T> = Result.failure(Exception("No attempt made"))
        while (attempt < maxRetries) {
            lastResult = block()
            if (lastResult.isSuccess)return lastResult
            val error = lastResult.exceptionOrNull()
            if (error is IllegalStateException || error is IllegalArgumentException) {
                return lastResult
            }
            val delayMs = baseDelayMs * (1L shl attempt)
            kotlinx.coroutines.delay(delayMs)
            attempt++
        }
        return lastResult
    }
}
