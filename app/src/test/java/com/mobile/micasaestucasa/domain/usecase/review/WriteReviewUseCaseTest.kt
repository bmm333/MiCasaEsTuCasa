package com.mobile.micasaestucasa.domain.usecase.review

import com.mobile.micasaestucasa.domain.model.booking.Booking
import com.mobile.micasaestucasa.domain.model.booking.BookingStatus
import com.mobile.micasaestucasa.domain.model.review.Review
import com.mobile.micasaestucasa.domain.model.review.ReviewType
import com.mobile.micasaestucasa.domain.repository.booking.BookingRepo
import com.mobile.micasaestucasa.domain.repository.review.ReviewRepo
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class WriteReviewUseCaseTest {
    private lateinit var reviewRepo: ReviewRepo
    private lateinit var bookingRepo: BookingRepo
    private lateinit var useCase: WriteReviewUseCase
    private val completedBooking = Booking(
        id = "booking123",
        propertyId = "prop123",
        renterId = "renter456",
        hostId = "host789",
        startDate = "2026-06-01",
        endDate = "2026-06-10",
        guestsCount = 2,
        pricePerDay = 100.0,
        totalPrice = 900.0,
        status = BookingStatus.COMPLETED,
        idempotencyKey = "key"
    )
    private val validPropertyReview = Review(
        id = "",
        bookingId = "booking123",
        reviewType = ReviewType.PROPERTY_REVIEW,
        authorId = "renter456",
        targetId = "prop123",
        propertyId = "prop123",
        title = "Soggiorno fantastico",
        body = "Casa bellissima, host disponibile",
        stars = 5
    )

    private val validRenterReview = Review(
        id          = "",
        bookingId   = "booking123",
        reviewType  = ReviewType.RENTER_REVIEW,
        authorId    = "host789",
        targetId    = "renter456",
        propertyId  = "prop123",
        title       = "Ospite eccellente",
        body        = "Lasciato tutto pulito e ordinato",
        stars       = 5
    )
    @Before
    fun setUp()
    {
        reviewRepo= mockk()
        bookingRepo=mockk()
        useCase= WriteReviewUseCase(reviewRepo,bookingRepo)
    }
    @Test
    fun `Property_review valid written correctly by renter`()= runTest {
        coEvery{bookingRepo.getBookingById("booking123")} returns Result.success(completedBooking)
        coEvery { reviewRepo.hasUserAlreadyReviewedBooking("renter456","booking123", ReviewType.PROPERTY_REVIEW) } returns Result.success(false)
        coEvery { reviewRepo.writeReview(any(),any()) } returns Result.success(Unit)
        val result=useCase(validPropertyReview,"renter456")
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { reviewRepo.writeReview(validPropertyReview,"renter456")  }
    }

    @Test
    fun `RENTER_Review valid written by host`()=runTest {
        coEvery{bookingRepo.getBookingById("booking123")} returns Result.success(completedBooking)
        coEvery { reviewRepo.hasUserAlreadyReviewedBooking("host789","booking123", ReviewType.RENTER_REVIEW) } returns Result.success(false)
        coEvery { reviewRepo.writeReview(any(),any()) } returns Result.success(Unit)
        val result=useCase(validRenterReview,"host789")
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { reviewRepo.writeReview(validRenterReview,"host789") }
    }
    @Test
    fun `userId blank ritorna failure senza chiamare repo`() = runTest {
        val result = useCase(validPropertyReview, "")
        assertTrue(result.isFailure)
        assertEquals("Id utente non puo essere vuoto", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { reviewRepo.writeReview(any(), any()) }
        coVerify(exactly = 0) { bookingRepo.getBookingById(any()) }
    }

    @Test
    fun `userId solo spazi ritorna failure`() = runTest {
        val result = useCase(validPropertyReview, "   ")
        assertTrue(result.isFailure)
        coVerify(exactly = 0) { bookingRepo.getBookingById(any()) }
    }

    @Test
    fun `titolo vuoto ritorna failure`() = runTest {
        val result = useCase(validPropertyReview.copy(title = ""), "renter456")
        assertTrue(result.isFailure)
        assertEquals("Titolo obbligatorio", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { bookingRepo.getBookingById(any()) }
    }
    @Test
    fun `body vuoto ritorna failure`() = runTest {
        val result = useCase(validPropertyReview.copy(body = ""), "renter456")
        assertTrue(result.isFailure)
        assertEquals("Descrizione obbligatoria", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { bookingRepo.getBookingById(any()) }
    }

    @Test
    fun `stelle 0 ritorna failure`() = runTest {
        val result = useCase(validPropertyReview.copy(stars = 0), "renter456")
        assertTrue(result.isFailure)
        assertEquals("Valutazione obbligatoria compresa tra 1 e 5", result.exceptionOrNull()?.message)
    }

    @Test
    fun `stelle 6 ritorna failure`() = runTest {
        val result = useCase(validPropertyReview.copy(stars = 6), "renter456")
        assertTrue(result.isFailure)
        assertEquals("Valutazione obbligatoria compresa tra 1 e 5", result.exceptionOrNull()?.message)
    }

    @Test
    fun `stelle negative ritorna failure`() = runTest {
        val result = useCase(validPropertyReview.copy(stars = -1), "renter456")
        assertTrue(result.isFailure)
    }

    @Test
    fun `bookingId vuoto ritorna failure`() = runTest {
        val result = useCase(validPropertyReview.copy(bookingId = ""), "renter456")
        assertTrue(result.isFailure)
        assertEquals("BookingId obbligatorio", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { bookingRepo.getBookingById(any()) }
    }

    @Test
    fun `targetId vuoto ritorna failure`() = runTest {
        val result = useCase(validPropertyReview.copy(targetId = ""), "renter456")
        assertTrue(result.isFailure)
        coVerify(exactly = 0) { bookingRepo.getBookingById(any()) }
    }
    @Test
    fun `booking non trovato ritorna failure`() = runTest {
        coEvery { bookingRepo.getBookingById(any()) } returns Result.failure(Exception("Booking not found"))
        val result = useCase(validPropertyReview, "renter456")
        assertTrue(result.isFailure)
        assertEquals("Booking not found", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { reviewRepo.writeReview(any(), any()) }
    }

    @Test
    fun `booking in stato REQUESTED ritorna failure`() = runTest {
        coEvery { bookingRepo.getBookingById(any()) } returns Result.success(completedBooking.copy(status = BookingStatus.REQUESTED))
        val result = useCase(validPropertyReview, "renter456")
        assertTrue(result.isFailure)
        assertEquals(
            "You can review the property once the stay is over",
            result.exceptionOrNull()?.message
        )
        coVerify(exactly = 0) { reviewRepo.writeReview(any(), any()) }
    }

    @Test
    fun `booking in stato ACCEPTED ritorna failure`() = runTest {
        coEvery { bookingRepo.getBookingById(any()) } returns Result.success(completedBooking.copy(status = BookingStatus.ACCEPTED))
        val result = useCase(validPropertyReview, "renter456")
        assertTrue(result.isFailure)
        coVerify(exactly = 0) { reviewRepo.writeReview(any(), any()) }
    }

    @Test
    fun `booking in stato CANCELLED ritorna failure`() = runTest {
        coEvery { bookingRepo.getBookingById(any()) } returns Result.success(completedBooking.copy(status = BookingStatus.CANCELLED))
        val result = useCase(validPropertyReview, "renter456")
        assertTrue(result.isFailure)
        coVerify(exactly = 0) { reviewRepo.writeReview(any(), any()) }
    }
    @Test
    fun `PROPERTY_REVIEW scritta dall host ritorna failure`() = runTest {
        coEvery { bookingRepo.getBookingById(any()) } returns
                Result.success(completedBooking)
        val result = useCase(validPropertyReview, "host789")
        assertTrue(result.isFailure)
        assertEquals(
            "Only the renter can leave a review in the property",
            result.exceptionOrNull()?.message
        )
        coVerify(exactly = 0) { reviewRepo.writeReview(any(), any()) }
    }

    @Test
    fun `PROPERTY_REVIEW con targetId diverso dalla propertyId del booking ritorna failure`() = runTest {
        coEvery { bookingRepo.getBookingById(any()) } returns Result.success(completedBooking)
        val tampered = validPropertyReview.copy(targetId = "altra-property")
        val result = useCase(tampered, "renter456")
        assertTrue(result.isFailure)
        assertEquals("Dose not match the property", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { reviewRepo.writeReview(any(), any()) }
    }
    @Test
    fun `RENTER_REVIEW scritta dal renter ritorna failure`() = runTest {
        coEvery { bookingRepo.getBookingById(any()) } returns Result.success(completedBooking)
        val result = useCase(validRenterReview, "renter456")
        assertTrue(result.isFailure)
        assertEquals("Only the host can review the renter", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { reviewRepo.writeReview(any(), any()) }
    }

    @Test
    fun `RENTER_REVIEW con targetId diverso dal renterId del booking ritorna failure`() = runTest {
        coEvery { bookingRepo.getBookingById(any()) } returns Result.success(completedBooking)
        val tampered = validRenterReview.copy(targetId = "altro-renter")
        val result = useCase(tampered, "host789")
        assertTrue(result.isFailure)
        assertEquals("Dose not match the renter", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { reviewRepo.writeReview(any(), any()) }
    }
    @Test
    fun `PROPERTY_REVIEW doppia per stesso booking ritorna failure`() = runTest {
        coEvery { bookingRepo.getBookingById(any()) } returns
                Result.success(completedBooking)
        coEvery { reviewRepo.hasUserAlreadyReviewedBooking("renter456", "booking123", ReviewType.PROPERTY_REVIEW)
        } returns Result.success(true)
        val result = useCase(validPropertyReview, "renter456")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("already given a review") == true)
        coVerify(exactly = 0) { reviewRepo.writeReview(any(), any()) }
    }

    @Test
    fun `RENTER_REVIEW doppia per stesso booking ritorna failure`() = runTest {
        coEvery { bookingRepo.getBookingById(any()) } returns
                Result.success(completedBooking)
        coEvery {
            reviewRepo.hasUserAlreadyReviewedBooking("host789", "booking123", ReviewType.RENTER_REVIEW) } returns Result.success(true)
        val result = useCase(validRenterReview, "host789")
        assertTrue(result.isFailure)
        coVerify(exactly = 0) { reviewRepo.writeReview(any(), any()) }
    }

    @Test
    fun `renter puo lasciare PROPERTY_REVIEW dopo aver gia lasciato RENTER_REVIEW — tipi diversi`() = runTest {
        coEvery { bookingRepo.getBookingById(any()) } returns Result.success(completedBooking)
        coEvery {
            reviewRepo.hasUserAlreadyReviewedBooking("renter456", "booking123", ReviewType.PROPERTY_REVIEW)
        } returns Result.success(false)  // PROPERTY_REVIEW non ancora scritta
        coEvery { reviewRepo.writeReview(any(), any()) } returns Result.success(Unit)
        val result = useCase(validPropertyReview, "renter456")
        assertTrue(result.isSuccess)
    }
    @Test
    fun `errore Firestore in writeReview propagato correttamente`() = runTest {
        coEvery { bookingRepo.getBookingById(any()) } returns Result.success(completedBooking)
        coEvery { reviewRepo.hasUserAlreadyReviewedBooking(any(), any(), any()) } returns Result.success(false)
        coEvery { reviewRepo.writeReview(any(), any()) } returns Result.failure(Exception("Firestore unavailable"))
        val result = useCase(validPropertyReview, "renter456")
        assertTrue(result.isFailure)
        assertEquals("Firestore unavailable", result.exceptionOrNull()?.message)
    }

    @Test
    fun `errore in hasUserAlreadyReviewedBooking trattato come no review — fallisce safe`() = runTest {
        coEvery { bookingRepo.getBookingById(any()) } returns Result.success(completedBooking)
        coEvery { reviewRepo.hasUserAlreadyReviewedBooking(any(), any(), any()) } returns Result.failure(Exception("Network error"))
        coEvery { reviewRepo.writeReview(any(), any()) } returns Result.success(Unit)
        val result = useCase(validPropertyReview, "renter456")
        assertTrue(result.isSuccess)
    }
}