package com.mobile.micasaestucasa.infra.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.mobile.micasaestucasa.domain.model.Booking
import com.mobile.micasaestucasa.domain.repository.BookingRepository
import kotlinx.coroutines.tasks.await

class FirebaseBookingRepository(
    private val firestore: FirebaseFirestore
) : BookingRepository {

    private val bookingsCollection = firestore.collection("bookings")

    override suspend fun getBooking(bookingId: String): Booking? {
        return try {
            bookingsCollection.document(bookingId).get().await().toObject(Booking::class.java)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getBookingsByUser(userId: String): List<Booking> {
        return try {
            bookingsCollection.whereEqualTo("userId", userId).get().await().toObjects(Booking::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun saveBooking(booking: Booking) {
        val docRef = if (booking.id.isEmpty()) {
            bookingsCollection.document()
        } else {
            bookingsCollection.document(booking.id)
        }

        val bookingToSave = if (booking.id.isEmpty()) {
            booking.copy(id = docRef.id)
        } else {
            booking
        }

        docRef.set(bookingToSave).await()
    }

    override suspend fun deleteBooking(bookingId: String) {
        bookingsCollection.document(bookingId).delete().await()
    }

    override suspend fun updateBooking(booking: Booking) {
        bookingsCollection.document(booking.id).set(booking).await()
    }


}
