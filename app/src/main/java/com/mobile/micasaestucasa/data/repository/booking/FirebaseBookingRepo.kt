package com.mobile.micasaestucasa.data.repository.booking

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.mobile.micasaestucasa.data.dto.booking.BookingDto
import com.mobile.micasaestucasa.data.mapper.booking.toDomain
import com.mobile.micasaestucasa.domain.model.booking.Booking
import com.mobile.micasaestucasa.domain.model.booking.BookingStatus
import com.mobile.micasaestucasa.domain.repository.booking.BookingRepo
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
/**
 * @author: Arben Mema
 *
 * Recap(not real documentation): In a real world scenario in a short
 * term rental marketplace like micasaestucasa, two renters can attempt to book the same
 * prop. for overlapping dates at the same momemnt. Without explicit protection
 * both requests would pass and perform writes to db concurrently, resulting in a double booking.
 *
 * Also ive handled the (un)stability problem of mobile clients networks, by retrying failed reqs.
 * Without idempotency protection, a retyr of a succesful-but-unACK'd booking req would create a
 * duplicate booking
 *
 * Finally, Firestore (As per Request of the course) is a globally distrb db with eventual consistency
 * as its def model. Op that span multiple doc's are not atomic unless explicitly wrapped inna transaction.
 *
 * Therefore i implemented a 3 layer consistnecy strategy 1 at domain level (Precheck), 2 Firestore with document level lock
 * and 3 Idempotency Key.
 *
 * !!!!!This is not the Real documentation(Algthough this is good material for the ADR, but im just writing down for now),
 * the development of this domain will be documented in detail in the next hours!!!
 * (IF DAYS have passed please assing me an issue on JIRA very probable i forgot)
 *
 * */
class FirebaseBookingRepo @Inject constructor(
    private val firestore: FirebaseFirestore
) : BookingRepo {
    private val bookingsCollection = firestore.collection("bookings")

    override suspend fun createBooking(
        booking: Booking,
        idempotencyKey: String
    ): Result<String> {
        return try {
            // primo controllo e quello del idempotency, cioe
            // se il key esiste gia ==> e un retry di una req gia processata
            val existing = bookingsCollection
                .whereEqualTo("idempotencyKey", idempotencyKey)
                .get().await()
            if (!existing.isEmpty) {
                // ritorna id del booking gia creato (safe retry)
                return Result.success(existing.documents.first().id)
            }
            // Usando Firestore Transaction (usa atomic rd + wr)
            // risolviamo il race condition senza lock
            var newBookingId = ""
            firestore.runTransaction { transaction ->
                // dentro la transazione leggo i overlapping
                val lockRef = firestore
                    .collection("property_locks")
                    .document(booking.propertyId)
                val lockDoc = transaction.get(lockRef)
                val lockedUntill = lockDoc.getLong("lockedUntill") ?: 0L
                if (lockedUntill > System.currentTimeMillis()) {
                    throw FirebaseFirestoreException(
                        "property locked",
                        FirebaseFirestoreException.Code.ABORTED
                    )
                }
                val docRef = bookingsCollection.document()
                newBookingId = docRef.id
                val dto = BookingDto(
                    id = newBookingId,
                    propertyId = booking.propertyId,
                    renterId = booking.renterId,
                    hostId = booking.hostId,
                    startDate = booking.startDate,
                    endDate = booking.endDate,
                    guestsCount = booking.guestsCount,
                    pricePerDay = booking.pricePerDay,
                    totalPrice = booking.totalPrice,
                    status = BookingStatus.REQUESTED.name,
                    idempotencyKey = idempotencyKey,
                    createdAt = System.currentTimeMillis()
                )
                transaction.set(docRef, dto)
                transaction.set(
                    lockRef,
                    mapOf(
                        "lockedUntill" to System.currentTimeMillis() + 30_000L,
                        "bookingId" to newBookingId
                    )
                )
            }.await()
            Result.success(newBookingId)
        } catch (e: FirebaseFirestoreException) {
            when (e.code) {
                // aborted = trabsactuib conflict, il caller puo fare retryF
                FirebaseFirestoreException.Code.ABORTED -> Result.failure(e)
                // UNAVALIBLE: Firestore dwn/latency spikes
                FirebaseFirestoreException.Code.UNAVAILABLE -> Result.failure(e)
                else -> Result.failure(e)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    override suspend fun hasOverlappingBooking(
        propertyId: String,
        startDate: String,
        endDate: String
    ): Result<Boolean> {
        return try {
            // q Firestore per booking attivi nella stessa prop.
            // FS no range overlap nativo su due campi
            // quindi filtriamo startDate<endDate richiesto
            // e endDate> startDate req
            val snapshot = bookingsCollection
                .whereEqualTo("propertyId", propertyId)
                .whereIn(
                    "status",
                    listOf(
                        BookingStatus.REQUESTED.name,
                        BookingStatus.ACCEPTED.name
                    )
                )
                .get().await()
            val hasOverlap = snapshot.documents.any {
                    doc ->
                val existingStart = doc.getString("startDate") ?: return@any false
                val existingEnd = doc.getString("endDate") ?: return@any false
                // overlap
                !(endDate<=existingStart||startDate>=existingEnd)
            }
            Result.success(hasOverlap)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    override suspend fun acceptBooking(
        bookingId: String,
        hostId: String
    ): Result<Unit> {
        return try {
            // tsx per garantie che solo host corretto posso accettare e che il booking sia ancora in REQ
            firestore.runTransaction {
                    transaction ->
                val docRef = bookingsCollection.document(bookingId)
                val doc = transaction.get(docRef)
                val currentHostId = doc.getString("hostId")
                val currentStatus = doc.getString("status")
                if (currentHostId != hostId) {
                    throw FirebaseFirestoreException(
                        "Non Autorizzato",
                        FirebaseFirestoreException.Code.PERMISSION_DENIED
                    )
                }
                if (currentStatus != BookingStatus.REQUESTED.name) {
                    throw FirebaseFirestoreException(
                        "Booking non in REQUESTED",
                        FirebaseFirestoreException.Code.FAILED_PRECONDITION
                    )
                }
                transaction.update(docRef, "status", BookingStatus.ACCEPTED.name)
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    override suspend fun rejectBooking(
        bookingId: String,
        hostId: String
    ): Result<Unit> {
        return try {
            firestore.runTransaction { transaction ->
                val docRef = bookingsCollection.document(bookingId)
                val doc = transaction.get(docRef)
                if (doc.getString("hostId") != hostId) {
                    throw FirebaseFirestoreException(
                        "Non Autorizzato",
                        FirebaseFirestoreException.Code.PERMISSION_DENIED
                    )
                }
                transaction.update(docRef, "status", BookingStatus.REJECTED.name)
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    override suspend fun cancelBooking(
        bookingId: String,
        userId: String
    ): Result<Unit> {
        return try {
            firestore.runTransaction { transaction ->
                val docRef = bookingsCollection.document(bookingId)
                val doc = transaction.get(docRef)
                val renterId = doc.getString("renterId")
                val hostId = doc.getString("hostId")
                val status = doc.getString("status")
                // solo renterid o hostid possono cancerlare
                if (renterId != userId && hostId != userId) {
                    throw FirebaseFirestoreException(
                        "Non autorizzato",
                        FirebaseFirestoreException.Code.PERMISSION_DENIED
                    )
                }
                // non si puo cancellare un booking gia completto
                if (status == BookingStatus.COMPLETED.name) {
                    throw FirebaseFirestoreException(
                        "Booking gia completato",
                        FirebaseFirestoreException.Code.FAILED_PRECONDITION
                    )
                }
                transaction.update(docRef, "status", BookingStatus.CANCELLED.name)
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getBookingsForRenter(renterId: String): Result<List<Booking>> {
        return try {
            val snapshot = bookingsCollection
                .whereEqualTo("renterId", renterId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get().await()
            Result.success(snapshot.documents.mapNotNull { it.toObject(BookingDto::class.java)?.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    override suspend fun getBookingsForHost(hostId: String): Result<List<Booking>> {
        return try {
            val snapshot = bookingsCollection
                .whereEqualTo("hostId", hostId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get().await()
            Result.success(
                snapshot.documents.mapNotNull {
                    it.toObject(BookingDto::class.java)?.toDomain()
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    override suspend fun getBookingById(bookingId: String): Result<Booking> {
        return try {
            val doc = bookingsCollection.document(bookingId).get().await()
            val booking = doc.toObject(BookingDto::class.java)?.toDomain()
                ?: return Result.failure(Exception("Booking non trovato"))
            Result.success(booking)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
