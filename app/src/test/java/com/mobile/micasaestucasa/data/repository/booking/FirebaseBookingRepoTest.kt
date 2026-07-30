package com.mobile.micasaestucasa.data.repository.booking

import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.Transaction
import com.mobile.micasaestucasa.data.dto.booking.BookingDto
import com.mobile.micasaestucasa.domain.model.booking.Booking
import com.mobile.micasaestucasa.domain.model.booking.BookingStatus
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FirebaseBookingRepoTest {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var bookingsCollection: CollectionReference
    private lateinit var repo: FirebaseBookingRepo

    @Before
    fun setUp() {
        firestore = mockk()
        bookingsCollection = mockk()
        every { firestore.collection("bookings") } returns bookingsCollection
        repo = FirebaseBookingRepo(firestore)
    }

    @Test
    fun `createBooking con idempotency key gia presente ritorna id esistente`() = runTest {
        val query = mockk<Query>()
        val snapshot = mockk<QuerySnapshot>()
        val existingDoc = mockk<DocumentSnapshot>()

        every { bookingsCollection.whereEqualTo("idempotencyKey", "idem-1") } returns query
        every { query.get() } returns Tasks.forResult(snapshot)
        every { snapshot.isEmpty } returns false
        every { snapshot.documents } returns listOf(existingDoc)
        every { existingDoc.id } returns "existing-booking"

        val result = repo.createBooking(sampleBooking(), "idem-1")

        assertTrue(result.isSuccess)
        assertEquals("existing-booking", result.getOrNull())
        verify(exactly = 0) { firestore.runTransaction(any<Transaction.Function<Any>>()) }
    }

    @Test
    fun `createBooking con errore transazione ritorna failure`() = runTest {
        val idempotencyQuery = mockk<Query>()
        val idempotencySnapshot = mockk<QuerySnapshot>()

        every { bookingsCollection.whereEqualTo("idempotencyKey", "idem-1") } returns idempotencyQuery
        every { idempotencyQuery.get() } returns Tasks.forResult(idempotencySnapshot)
        every { idempotencySnapshot.isEmpty } returns true
        every {
            firestore.runTransaction(any<Transaction.Function<Any>>())
        } returns Tasks.forException(IllegalStateException("transaction failed"))

        val result = repo.createBooking(sampleBooking(), "idem-1")

        assertTrue(result.isFailure)
        assertEquals("transaction failed", result.exceptionOrNull()?.message)
    }

    @Test
    fun `createBooking success crea dto e ritorna new id`() = runTest {
        val idempotencyQuery = mockk<Query>()
        val idempotencySnapshot = mockk<QuerySnapshot>()
        val transaction = mockk<Transaction>()
        val lockCollection = mockk<CollectionReference>()
        val lockRef = mockk<DocumentReference>()
        val lockDoc = mockk<DocumentSnapshot>()
        val bookingDoc = mockk<DocumentReference>()
        val propertyCollection = mockk<CollectionReference>()
        val propertyRef = mockk<DocumentReference>()
        val propertyDoc = mockk<DocumentSnapshot>()

        every { bookingsCollection.whereEqualTo("idempotencyKey", "idem-1") } returns idempotencyQuery
        every { idempotencyQuery.get() } returns Tasks.forResult(idempotencySnapshot)
        every { idempotencySnapshot.isEmpty } returns true

        every { firestore.collection("property_locks") } returns lockCollection
        every { lockCollection.document("prop123") } returns lockRef
        every { transaction.get(lockRef) } returns lockDoc
        every { lockDoc.getLong("lockedUntill") } returns 0L

        every { firestore.collection("properties") } returns propertyCollection
        every { propertyCollection.document("prop123") } returns propertyRef
        every { transaction.get(propertyRef) } returns propertyDoc
        every { propertyDoc.getBoolean("isOnHold") } returns false

        every { bookingsCollection.document() } returns bookingDoc
        every { bookingDoc.id } returns "new-booking-id"

        every { transaction.set(bookingDoc, any<BookingDto>()) } returns transaction
        every { transaction.set(lockRef, any<Map<String, Any>>()) } returns transaction

        stubRunTransactionExecuting(transaction)

        val result = repo.createBooking(sampleBooking(), "idem-1")

        assertTrue(result.isSuccess)
        assertEquals("new-booking-id", result.getOrNull())
        verify(exactly = 1) {
            transaction.set(
                bookingDoc,
                match<BookingDto> { dto ->
                    dto.id == "new-booking-id" &&
                        dto.propertyId == "prop123" &&
                        dto.status == BookingStatus.REQUESTED.name &&
                        dto.idempotencyKey == "idem-1"
                }
            )
        }
    }

    @Test
    fun `createBooking con errore su lookup idempotency ritorna failure`() = runTest {
        val idempotencyQuery = mockk<Query>()

        every { bookingsCollection.whereEqualTo("idempotencyKey", "idem-1") } returns idempotencyQuery
        every {
            idempotencyQuery.get()
        } returns Tasks.forException(IllegalStateException("idempotency lookup failed"))

        val result = repo.createBooking(sampleBooking(), "idem-1")

        assertTrue(result.isFailure)
        assertEquals("idempotency lookup failed", result.exceptionOrNull()?.message)
    }

    @Test
    fun `hasOverlappingBooking ritorna true quando date si sovrappongono`() = runTest {
        val propertyQuery = mockk<Query>()
        val statusQuery = mockk<Query>()
        val snapshot = mockk<QuerySnapshot>()
        val doc = mockk<DocumentSnapshot>()

        every { bookingsCollection.whereEqualTo("propertyId", "prop123") } returns propertyQuery
        every {
            propertyQuery.whereIn(
                "status",
                listOf(BookingStatus.REQUESTED.name, BookingStatus.ACCEPTED.name)
            )
        } returns statusQuery
        every { statusQuery.get() } returns Tasks.forResult(snapshot)
        every { snapshot.documents } returns listOf(doc)
        every { doc.getString("startDate") } returns "2026-07-05"
        every { doc.getString("endDate") } returns "2026-07-12"

        val result = repo.hasOverlappingBooking("prop123", "2026-07-07", "2026-07-10")

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull() == true)
    }

    @Test
    fun `hasOverlappingBooking ritorna false quando non ci sono overlap`() = runTest {
        val propertyQuery = mockk<Query>()
        val statusQuery = mockk<Query>()
        val snapshot = mockk<QuerySnapshot>()
        val doc = mockk<DocumentSnapshot>()

        every { bookingsCollection.whereEqualTo("propertyId", "prop123") } returns propertyQuery
        every {
            propertyQuery.whereIn(
                "status",
                listOf(BookingStatus.REQUESTED.name, BookingStatus.ACCEPTED.name)
            )
        } returns statusQuery
        every { statusQuery.get() } returns Tasks.forResult(snapshot)
        every { snapshot.documents } returns listOf(doc)
        every { doc.getString("startDate") } returns "2026-07-20"
        every { doc.getString("endDate") } returns "2026-07-25"

        val result = repo.hasOverlappingBooking("prop123", "2026-07-07", "2026-07-10")

        assertTrue(result.isSuccess)
        assertFalse(result.getOrNull() == true)
    }

    @Test
    fun `hasOverlappingBooking ignora documenti senza date`() = runTest {
        val propertyQuery = mockk<Query>()
        val statusQuery = mockk<Query>()
        val snapshot = mockk<QuerySnapshot>()
        val doc = mockk<DocumentSnapshot>()

        every { bookingsCollection.whereEqualTo("propertyId", "prop123") } returns propertyQuery
        every {
            propertyQuery.whereIn(
                "status",
                listOf(BookingStatus.REQUESTED.name, BookingStatus.ACCEPTED.name)
            )
        } returns statusQuery
        every { statusQuery.get() } returns Tasks.forResult(snapshot)
        every { snapshot.documents } returns listOf(doc)
        every { doc.getString("startDate") } returns null
        every { doc.getString("endDate") } returns null

        val result = repo.hasOverlappingBooking("prop123", "2026-07-07", "2026-07-10")

        assertTrue(result.isSuccess)
        assertEquals(false, result.getOrNull())
    }

    @Test
    fun `hasOverlappingBooking propaga errore query`() = runTest {
        val propertyQuery = mockk<Query>()
        val statusQuery = mockk<Query>()

        every { bookingsCollection.whereEqualTo("propertyId", "prop123") } returns propertyQuery
        every {
            propertyQuery.whereIn(
                "status",
                listOf(BookingStatus.REQUESTED.name, BookingStatus.ACCEPTED.name)
            )
        } returns statusQuery
        every { statusQuery.get() } returns Tasks.forException(IllegalStateException("offline"))

        val result = repo.hasOverlappingBooking("prop123", "2026-07-07", "2026-07-10")

        assertTrue(result.isFailure)
        assertEquals("offline", result.exceptionOrNull()?.message)
    }

    @Test
    fun `acceptBooking success aggiorna stato ad accepted`() = runTest {
        val transaction = mockk<Transaction>()
        val docRef = mockk<DocumentReference>()
        val doc = mockk<DocumentSnapshot>()

        every { bookingsCollection.document("booking-1") } returns docRef
        every { transaction.get(docRef) } returns doc
        every { doc.getString("hostId") } returns "host-1"
        every { doc.getString("status") } returns BookingStatus.REQUESTED.name
        every { transaction.update(docRef, "status", BookingStatus.ACCEPTED.name) } returns transaction
        stubRunTransactionExecuting(transaction)

        val result = repo.acceptBooking("booking-1", "host-1")

        assertTrue(result.isSuccess)
        verify(exactly = 1) { transaction.update(docRef, "status", BookingStatus.ACCEPTED.name) }
    }

    @Test
    fun `acceptBooking con host diverso ritorna failure`() = runTest {
        every {
            firestore.runTransaction(any<Transaction.Function<Any>>())
        } returns Tasks.forException(IllegalStateException("tx failed"))

        val result = repo.acceptBooking("booking-1", "host-1")

        assertTrue(result.isFailure)
        assertEquals("tx failed", result.exceptionOrNull()?.message)
    }

    @Test
    fun `acceptBooking con stato non requested ritorna failure`() = runTest {
        every {
            firestore.runTransaction(any<Transaction.Function<Any>>())
        } returns Tasks.forException(IllegalStateException("status invalid"))

        val result = repo.acceptBooking("booking-1", "host-1")

        assertTrue(result.isFailure)
        assertEquals("status invalid", result.exceptionOrNull()?.message)
    }

    @Test
    fun `rejectBooking success aggiorna stato a rejected`() = runTest {
        val transaction = mockk<Transaction>()
        val docRef = mockk<DocumentReference>()
        val doc = mockk<DocumentSnapshot>()

        every { bookingsCollection.document("booking-1") } returns docRef
        every { transaction.get(docRef) } returns doc
        every { doc.getString("hostId") } returns "host-1"
        every { transaction.update(docRef, "status", BookingStatus.REJECTED.name) } returns transaction
        stubRunTransactionExecuting(transaction)

        val result = repo.rejectBooking("booking-1", "host-1")

        assertTrue(result.isSuccess)
    }

    @Test
    fun `rejectBooking con host non autorizzato ritorna failure`() = runTest {
        every {
            firestore.runTransaction(any<Transaction.Function<Any>>())
        } returns Tasks.forException(IllegalStateException("host mismatch"))

        val result = repo.rejectBooking("booking-1", "host-1")

        assertTrue(result.isFailure)
        assertEquals("host mismatch", result.exceptionOrNull()?.message)
    }

    @Test
    fun `cancelBooking success quando user e renter`() = runTest {
        val transaction = mockk<Transaction>()
        val docRef = mockk<DocumentReference>()
        val doc = mockk<DocumentSnapshot>()

        every { bookingsCollection.document("booking-1") } returns docRef
        every { transaction.get(docRef) } returns doc
        every { doc.getString("renterId") } returns "user-1"
        every { doc.getString("hostId") } returns "host-1"
        every { doc.getString("status") } returns BookingStatus.REQUESTED.name
        every { transaction.update(docRef, "status", BookingStatus.CANCELLED.name) } returns transaction
        stubRunTransactionExecuting(transaction)

        val result = repo.cancelBooking("booking-1", "user-1")

        assertTrue(result.isSuccess)
    }

    @Test
    fun `cancelBooking con user non autorizzato ritorna failure`() = runTest {
        every {
            firestore.runTransaction(any<Transaction.Function<Any>>())
        } returns Tasks.forException(IllegalStateException("not authorized"))

        val result = repo.cancelBooking("booking-1", "user-999")

        assertTrue(result.isFailure)
        assertEquals("not authorized", result.exceptionOrNull()?.message)
    }

    @Test
    fun `cancelBooking su booking completato ritorna failure`() = runTest {
        every {
            firestore.runTransaction(any<Transaction.Function<Any>>())
        } returns Tasks.forException(IllegalStateException("booking completed"))

        val result = repo.cancelBooking("booking-1", "user-1")

        assertTrue(result.isFailure)
        assertEquals("booking completed", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getBookingsForRenter ritorna solo documenti convertibili`() = runTest {
        val renterQuery = mockk<Query>()
        val orderedQuery = mockk<Query>()
        val snapshot = mockk<QuerySnapshot>()
        val doc1 = mockk<DocumentSnapshot>()
        val doc2 = mockk<DocumentSnapshot>()

        every { bookingsCollection.whereEqualTo("renterId", "renter-1") } returns renterQuery
        every { renterQuery.orderBy("createdAt", Query.Direction.DESCENDING) } returns orderedQuery
        every { orderedQuery.get() } returns Tasks.forResult(snapshot)
        every { snapshot.documents } returns listOf(doc1, doc2)
        every { doc1.toObject(BookingDto::class.java) } returns sampleBookingDto(id = "b1")
        every { doc2.toObject(BookingDto::class.java) } returns null

        val result = repo.getBookingsForRenter("renter-1")

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        assertEquals("b1", result.getOrNull()?.first()?.id)
    }

    @Test
    fun `getBookingsForHost ritorna solo documenti convertibili`() = runTest {
        val hostQuery = mockk<Query>()
        val orderedQuery = mockk<Query>()
        val snapshot = mockk<QuerySnapshot>()
        val doc1 = mockk<DocumentSnapshot>()
        val doc2 = mockk<DocumentSnapshot>()

        every { bookingsCollection.whereEqualTo("hostId", "host-1") } returns hostQuery
        every { hostQuery.orderBy("createdAt", Query.Direction.DESCENDING) } returns orderedQuery
        every { orderedQuery.get() } returns Tasks.forResult(snapshot)
        every { snapshot.documents } returns listOf(doc1, doc2)
        every { doc1.toObject(BookingDto::class.java) } returns sampleBookingDto(id = "b1")
        every { doc2.toObject(BookingDto::class.java) } returns null

        val result = repo.getBookingsForHost("host-1")

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        assertEquals("b1", result.getOrNull()?.first()?.id)
    }

    @Test
    fun `getBookingById con dto valido ritorna booking`() = runTest {
        val docRef = mockk<DocumentReference>()
        val doc = mockk<DocumentSnapshot>()

        every { bookingsCollection.document("b1") } returns docRef
        every { docRef.get() } returns Tasks.forResult(doc)
        every { doc.toObject(BookingDto::class.java) } returns sampleBookingDto(id = "b1")

        val result = repo.getBookingById("b1")

        assertTrue(result.isSuccess)
        assertEquals("b1", result.getOrNull()?.id)
    }

    @Test
    fun `getBookingById con dto nullo ritorna booking non trovato`() = runTest {
        val docRef = mockk<DocumentReference>()
        val doc = mockk<DocumentSnapshot>()

        every { bookingsCollection.document("missing") } returns docRef
        every { docRef.get() } returns Tasks.forResult(doc)
        every { doc.toObject(BookingDto::class.java) } returns null

        val result = repo.getBookingById("missing")

        assertTrue(result.isFailure)
        assertEquals("Booking non trovato", result.exceptionOrNull()?.message)
    }

    @Suppress("UNCHECKED_CAST")
    private fun stubRunTransactionExecuting(transaction: Transaction) {
        every { firestore.runTransaction(any<Transaction.Function<Any>>()) } answers {
            val function = firstArg<Transaction.Function<Any>>()
            try {
                function.apply(transaction)
                Tasks.forResult(mockk(relaxed = true))
            } catch (e: Exception) {
                Tasks.forException(e)
            }
        }
    }

    private fun sampleBooking(): Booking {
        return Booking(
            id = "",
            propertyId = "prop123",
            renterId = "renter456",
            hostId = "host789",
            startDate = "2026-07-01",
            endDate = "2026-07-10",
            guestsCount = 2,
            pricePerDay = 100.0,
            status = BookingStatus.REQUESTED,
            totalPrice = 900.0,
            idempotencyKey = "idem-1",
            createdAt = 123456789L
        )
    }

    private fun sampleBookingDto(id: String): BookingDto {
        return BookingDto(
            id = id,
            propertyId = "prop123",
            renterId = "renter456",
            hostId = "host789",
            startDate = "2026-07-01",
            endDate = "2026-07-10",
            guestsCount = 2,
            pricePerDay = 100.0,
            totalPrice = 900.0,
            status = BookingStatus.REQUESTED.name,
            idempotencyKey = "idem-1",
            createdAt = 123456789L
        )
    }
}
