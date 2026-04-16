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

class AcceptBookingUseCaseTest {
    private lateinit var bookingRepo: BookingRepo
    private lateinit var acceptBookingUseCase: AcceptBookingUseCase

    @Before
    fun setUp() {
        bookingRepo = mockk()
        acceptBookingUseCase = AcceptBookingUseCase(bookingRepo)
    }

    @Test
    fun `accept con dati validi ritorna successo`() = runTest {
        coEvery { bookingRepo.acceptBooking("booking123", "host456") } returns Result.success(Unit)

        val result = acceptBookingUseCase("booking123", "host456")

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { bookingRepo.acceptBooking("booking123", "host456") }
    }

    @Test
    fun `accept con bookingId vuoto ritorna failure e non chiama repo`() = runTest {
        val result = acceptBookingUseCase("", "host456")

        assertTrue(result.isFailure)
        assertEquals("bookingId obbligatorio", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { bookingRepo.acceptBooking(any(), any()) }
    }

    @Test
    fun `accept con bookingId blank ritorna failure e non chiama repo`() = runTest {
        val result = acceptBookingUseCase("   ", "host456")

        assertTrue(result.isFailure)
        assertEquals("bookingId obbligatorio", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { bookingRepo.acceptBooking(any(), any()) }
    }

    @Test
    fun `accept con hostId vuoto ritorna failure e non chiama repo`() = runTest {
        val result = acceptBookingUseCase("booking123", "")

        assertTrue(result.isFailure)
        assertEquals("hostId obbligatorio", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { bookingRepo.acceptBooking(any(), any()) }
    }

    @Test
    fun `accept con hostId blank ritorna failure e non chiama repo`() = runTest {
        val result = acceptBookingUseCase("booking123", "   ")

        assertTrue(result.isFailure)
        assertEquals("hostId obbligatorio", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { bookingRepo.acceptBooking(any(), any()) }
    }

    @Test
    fun `accept propaga errore del repository`() = runTest {
        coEvery {
            bookingRepo.acceptBooking("booking123", "host456")
        } returns Result.failure(IllegalStateException("booking gia processato"))

        val result = acceptBookingUseCase("booking123", "host456")

        assertTrue(result.isFailure)
        assertEquals("booking gia processato", result.exceptionOrNull()?.message)
        coVerify(exactly = 1) { bookingRepo.acceptBooking("booking123", "host456") }
    }
}
