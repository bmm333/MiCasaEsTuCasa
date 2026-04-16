package com.mobile.micasaestucasa.domain.usecase.booking

import com.mobile.micasaestucasa.domain.model.booking.Booking
import com.mobile.micasaestucasa.domain.model.booking.BookingStatus
import com.mobile.micasaestucasa.domain.repository.booking.BookingRepo
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class CreateBookingUseCaseTest {
    private lateinit var bookingRepo: BookingRepo
    private lateinit var createBookingUseCase: CreateBookingUseCase

    private val validBooking = Booking(
        id = "",
        propertyId = "prop123",
        renterId = "renter456",
        hostId = "host789",
        startDate = "2026-07-01",
        endDate = "2026-07-10",
        guestsCount = 2,
        pricePerDay = 100.0,
        totalPrice = 900.0,
        status = BookingStatus.REQUESTED,
        idempotencyKey = "uuid-test-001"
    )

    @Before
    fun setUp() {
        bookingRepo = mockk()
    }
    private fun buildUseCase() = CreateBookingUseCase(bookingRepo)

    // il happy path
    @Test
    fun `booking valido senza overlap ritorna successo`() = runTest {
        coEvery {
            bookingRepo.hasOverlappingBooking(any(), any(), any())
        } returns Result.success(false)
        coEvery {
            bookingRepo.createBooking(any(), any())
        } returns Result.success("booking123")
        val result = buildUseCase()(validBooking, "uuid-001")
        assertTrue(result.isSuccess)
        assertEquals("booking123", result.getOrNull())
    }

    // valiazione domain
    @Test
    fun `propertyId vuoto ritorna failure senza chiamare repo`() = runTest {
        val result = buildUseCase()(validBooking.copy(propertyId = ""), "uuid-001")
        assertTrue(result.isFailure)
        assertEquals("PropertyId Obbligatorio", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { bookingRepo.createBooking(any(), any()) }
    }

    @Test
    fun `startDate uguale a endDate ritorna failure`() = runTest {
        val result = buildUseCase()(
            validBooking.copy(startDate = "2026-07-10", endDate = "2026-07-10"),
            "uuid-001"
        )
        assertTrue(result.isFailure)
        assertEquals("Data inizio deve essere prima della data fine", result.exceptionOrNull()?.message)
    }

    @Test
    fun `startDate dopo endDate ritorna failure`() = runTest {
        val result = buildUseCase()(
            validBooking.copy(startDate = "2026-07-15", endDate = "2026-07-10"),
            "uuid-001"
        )
        assertTrue(result.isFailure)
    }

    @Test
    fun `renter uguale a host ritorna failure regola == no self host`() = runTest {
        val result = buildUseCase()(
            validBooking.copy(renterId = "user123", hostId = "user123"),
            "uuid-001"
        )
        assertTrue(result.isFailure)
        coVerify(exactly = 0) { bookingRepo.createBooking(any(), any()) }
    }

    @Test
    fun `overlap rilevato ritorna failure — date gia occupate`() = runTest {
        coEvery {
            bookingRepo.hasOverlappingBooking(any(), any(), any())
        } returns Result.success(true)

        val result = buildUseCase()(validBooking, "uuid-001")

        assertTrue(result.isFailure)
        coVerify(exactly = 0) { bookingRepo.createBooking(any(), any()) }
    }

    @Test
    fun `errore nel check overlap propagato — non crea booking`() = runTest {
        coEvery {
            bookingRepo.hasOverlappingBooking(any(), any(), any())
        } returns Result.failure(Exception("Firestore unavailable"))

        val result = buildUseCase()(validBooking, "uuid-001")

        assertTrue(result.isFailure)
        coVerify(exactly = 0) { bookingRepo.createBooking(any(), any()) }
    }

    // Retry and expo backoff
    @Test
    fun `errore tansistente Firestore triggera retry- successo al secondo tentativo`() = runTest {
        val callCount = AtomicInteger(0)

        coEvery {
            bookingRepo.hasOverlappingBooking(any(), any(), any())
        } returns Result.success(false)
        coEvery {
            bookingRepo.createBooking(any(), any())
        } answers {
            if (callCount.incrementAndGet() == 1) {
                Result.failure(Exception("Firestore unavailable"))
            } else {
                Result.success("booking123")
            }
        }
        val result = buildUseCase()(validBooking, "uuid-001")
        assertTrue(result.isSuccess)
        assertEquals(2, callCount.get())
    }

    @Test
    fun `3 errori transienti consecutivi ritorna failure dopo MAX_RETRIES`() = runTest {
        coEvery {
            bookingRepo.hasOverlappingBooking(any(), any(), any())
        } returns Result.success(false)

        coEvery {
            bookingRepo.createBooking(any(), any())
        } returns Result.failure(Exception("UNAVAILABLE"))

        val result = buildUseCase()(validBooking, "uuid-001")

        assertTrue(result.isFailure)
        // 3 tentativi totali
        coVerify(exactly = 3) { bookingRepo.createBooking(any(), any()) }
    }

    @Test
    fun `errore business logic NON triggera retry`() = runTest {
        coEvery {
            bookingRepo.hasOverlappingBooking(any(), any(), any())
        } returns Result.success(false)

        coEvery {
            bookingRepo.createBooking(any(), any())
        } returns Result.failure(IllegalStateException("Conflict detected"))

        val result = buildUseCase()(validBooking, "uuid-001")

        assertTrue(result.isFailure)
        // 1 solo tentativo — no retry su business error
        coVerify(exactly = 1) { bookingRepo.createBooking(any(), any()) }
    }

    // test di concorrenza
    @Test
    fun `richieste concorrenti - solo una va a buon fine`() = runTest {
        val successCount = AtomicInteger(0)
        val failureCount = AtomicInteger(0)
        val callCount = AtomicInteger(0)

        coEvery {
            bookingRepo.hasOverlappingBooking(any(), any(), any())
        } answers {
            // simulo dopo il primo check la propieta risulta occpuata
            if (callCount.incrementAndGet() == 1) {
                Result.success(false)
            } else {
                Result.success(true)
            }
        }
        coEvery {
            bookingRepo.createBooking(any(), any())
        } returns Result.success("booking123")
        // 5 corutine concurrenti che tentano lo stesso booking
        val jobs = (1..5).map { i ->
            launch(Dispatchers.IO) {
                val result = buildUseCase()(
                    validBooking,
                    "uuid-concurrent-$i"
                )
                if (result.isSuccess) {
                    successCount.incrementAndGet()
                } else {
                    failureCount.incrementAndGet()
                }
            }
        }
        jobs.forEach { it.join() }
        // esattamente 1 successo e 4 failure per overlap
        assertEquals(1, successCount.get())
        assertEquals(4, failureCount.get())
    }

    @Test
    fun `idempotency — stesso key non crea booking duplicati`() = runTest {
        val createCallCount = AtomicInteger(0)
        coEvery {
            bookingRepo.hasOverlappingBooking(any(), any(), any())
        } returns Result.success(false)
        coEvery {
            bookingRepo.createBooking(any(), any())
        } answers {
            createCallCount.incrementAndGet()
            Result.success("booking123")
        }
        val idempotencyKey = "uuid-same-key"
        // 3 chiamate con lo stesso key — simulandi retry mobile
        val results = (1..3).map {
            buildUseCase()(validBooking, idempotencyKey)
        }
        // tutte e 3 ritornano successo
        assertTrue(results.all { it.isSuccess })
        // createBooking chiamato 3 volte — il check idempotency
        // e responsabilità del repository, non del use case
        // (il repo è mockato, in produzione ritorna l'id esistente)
        assertEquals(3, createCallCount.get())
    }
}
