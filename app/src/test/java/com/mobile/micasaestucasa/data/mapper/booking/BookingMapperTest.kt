package com.mobile.micasaestucasa.data.mapper.booking

import com.mobile.micasaestucasa.data.dto.booking.BookingDto
import com.mobile.micasaestucasa.domain.model.booking.Booking
import com.mobile.micasaestucasa.domain.model.booking.BookingStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class BookingMapperTest {

    @Test
    fun `toDomain con campi null applica default coerenti`() {
        val dto = BookingDto()

        val result = dto.toDomain()

        assertEquals("", result.id)
        assertEquals("", result.propertyId)
        assertEquals("", result.renterId)
        assertEquals("", result.hostId)
        assertEquals("", result.startDate)
        assertEquals("", result.endDate)
        assertEquals(1, result.guestsCount)
        assertEquals(0.0, result.pricePerDay, 0.0)
        assertEquals(0.0, result.totalPrice, 0.0)
        assertEquals(BookingStatus.REQUESTED, result.status)
        assertEquals("", result.idempotencyKey)
        assertEquals(0L, result.createdAt)
    }

    @Test
    fun `toDomain con status valorizzato mappa enum corretto`() {
        val dto = BookingDto(
            id = "b1",
            propertyId = "p1",
            renterId = "r1",
            hostId = "h1",
            startDate = "2026-07-01",
            endDate = "2026-07-10",
            guestsCount = 2,
            pricePerDay = 100.0,
            totalPrice = 900.0,
            status = "ACCEPTED",
            idempotencyKey = "idem-1",
            createdAt = 123L
        )

        val result = dto.toDomain()

        assertEquals(BookingStatus.ACCEPTED, result.status)
        assertEquals("b1", result.id)
        assertEquals(123L, result.createdAt)
    }

    @Test
    fun `toDto mantiene tutti i valori del domain model`() {
        val booking = Booking(
            id = "b1",
            propertyId = "p1",
            renterId = "r1",
            hostId = "h1",
            startDate = "2026-07-01",
            endDate = "2026-07-10",
            guestsCount = 2,
            pricePerDay = 100.0,
            status = BookingStatus.REQUESTED,
            totalPrice = 900.0,
            idempotencyKey = "idem-1",
            createdAt = 123456789L
        )

        val dto = booking.toDto()

        assertEquals("b1", dto.id)
        assertEquals("p1", dto.propertyId)
        assertEquals("r1", dto.renterId)
        assertEquals("h1", dto.hostId)
        assertEquals("2026-07-01", dto.startDate)
        assertEquals("2026-07-10", dto.endDate)
        assertEquals(2, dto.guestsCount)
        assertEquals(100.0, dto.pricePerDay)
        assertEquals(900.0, dto.totalPrice)
        assertEquals("REQUESTED", dto.status)
        assertEquals("idem-1", dto.idempotencyKey)
        assertEquals(123456789L, dto.createdAt)
    }
}
