package com.mobile.micasaestucasa.domain.usecase.booking

import com.mobile.micasaestucasa.domain.repository.booking.BookingRepo
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CancelBookingUseCaseTest {
    private lateinit var bookingRepo: BookingRepo
    private lateinit var cancelBookingUseCase: CancelBookingUseCase

    @Before
    fun setUp() {
        bookingRepo = mockk()
        cancelBookingUseCase = CancelBookingUseCase(bookingRepo)
    }

    @Test
    fun `cancel con dati validi ritorna successo`() = runTest {
        coEvery { bookingRepo.cancelBooking("booking123", "user456") } returns Result.success(Unit)

        val result = cancelBookingUseCase("booking123", "user456")

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { bookingRepo.cancelBooking("booking123", "user456") }
    }

    @Test
    fun `cancel con bookingId vuoto ritorna failure e non chiama repo`() = runTest {
        val result = cancelBookingUseCase("", "user456")

        assertTrue(result.isFailure)
        assertEquals("bookingId obbligatorio", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { bookingRepo.cancelBooking(any(), any()) }
    }

    @Test
    fun `cancel con bookingId blank ritorna failure e non chiama repo`() = runTest {
        val result = cancelBookingUseCase("   ", "user456")

        assertTrue(result.isFailure)
        assertEquals("bookingId obbligatorio", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { bookingRepo.cancelBooking(any(), any()) }
    }

    @Test
    fun `cancel con userId vuoto ritorna failure e non chiama repo`() = runTest {
        val result = cancelBookingUseCase("booking123", "")

        assertTrue(result.isFailure)
        assertEquals("userId obbligatorio", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { bookingRepo.cancelBooking(any(), any()) }
    }

    @Test
    fun `cancel con userId blank ritorna failure e non chiama repo`() = runTest {
        val result = cancelBookingUseCase("booking123", "   ")

        assertTrue(result.isFailure)
        assertEquals("userId obbligatorio", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { bookingRepo.cancelBooking(any(), any()) }
    }

    @Test
    fun `cancel propaga errore del repository`() = runTest {
        coEvery {
            bookingRepo.cancelBooking("booking123", "user456")
        } returns Result.failure(IllegalStateException("stato booking non cancellabile"))

        val result = cancelBookingUseCase("booking123", "user456")

        assertTrue(result.isFailure)
        assertEquals("stato booking non cancellabile", result.exceptionOrNull()?.message)
        coVerify(exactly = 1) { bookingRepo.cancelBooking("booking123", "user456") }
    }
}
