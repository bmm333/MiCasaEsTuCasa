package com.mobile.micasaestucasa.domain.usecase.booking

import com.mobile.micasaestucasa.domain.model.booking.Booking
import com.mobile.micasaestucasa.domain.repository.booking.BookingRepo
import javax.inject.Inject

class CreateBookingUseCase @Inject constructor(
    private val bookingRepo: BookingRepo
) {
    companion object {
        private const val MAX_RETRIES = 3
        private const val BASE_DELAY_MS = 500L
    }
    suspend operator fun invoke(
        booking: Booking,
        idempotencyKey: String
    ): Result<String> {
        // validazione domain
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
            // no retry su errori di bussines logic
            if (error is IllegalStateException || error is IllegalArgumentException) {
                return lastResult
            }
            // exp backoff: 500ms->1000ms->2000ms
            val delayMs = baseDelayMs * (1L shl attempt)
            kotlinx.coroutines.delay(delayMs)
            attempt++
        }
        return lastResult
    }
}
