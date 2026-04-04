package com.mobile.micasaestucasa.data.dto.property

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class PropertyDtoTest {

    @Test
    fun `copy e component mantengono valori su dto complesso`() {
        val dto = PropertyDto(
            id = "p1",
            ownerId = "owner1",
            title = "Loft",
            description = "Centro",
            latitude = 45.0,
            longitude = 9.0,
            city = "Milano",
            pricePerDay = 150.0,
            capacity = 4,
            keywords = listOf("wifi"),
            imageUrls = listOf("img1"),
            availableFrom = "2026-07-01",
            availableTo = "2026-07-31",
            rating = 4.8,
            reviewsCount = 30
        )

        val modified = dto.copy(city = "Torino")

        assertEquals("p1", dto.component1())
        assertEquals("owner1", dto.component2())
        assertEquals("Loft", dto.component3())
        assertEquals("Centro", dto.component4())
        assertEquals(45.0, dto.component5())
        assertEquals(9.0, dto.component6())
        assertEquals("Milano", dto.component7())
        assertEquals(150.0, dto.component8())
        assertEquals(4, dto.component9())
        assertEquals(listOf("wifi"), dto.component10())
        assertEquals(listOf("img1"), dto.component11())
        assertEquals("2026-07-01", dto.component12())
        assertEquals("2026-07-31", dto.component13())
        assertEquals(4.8, dto.component14())
        assertEquals(30, dto.component15())
        assertEquals("Torino", modified.city)
        assertNotEquals(dto, modified)
    }
}
