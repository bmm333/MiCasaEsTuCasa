package com.mobile.micasaestucasa.data.dto.booking

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class BookingDtoTest {

    @Test
    fun `copy e component mantengono valori su booking dto`() {
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
            status = "REQUESTED",
            idempotencyKey = "idem-1",
            createdAt = 123456789L
        )

        val modified = dto.copy(status = "ACCEPTED")

        assertEquals("b1", dto.component1())
        assertEquals("p1", dto.component2())
        assertEquals("r1", dto.component3())
        assertEquals("h1", dto.component4())
        assertEquals("2026-07-01", dto.component5())
        assertEquals("2026-07-10", dto.component6())
        assertEquals(2, dto.component7())
        assertEquals(100.0, dto.component8())
        assertEquals(900.0, dto.component9())
        assertEquals("REQUESTED", dto.component10())
        assertEquals("idem-1", dto.component11())
        assertEquals(123456789L, dto.component12())

        assertEquals("ACCEPTED", modified.status)
        assertNotEquals(dto, modified)
    }
}
