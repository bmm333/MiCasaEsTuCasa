package com.mobile.micasaestucasa.data.mapper.property

import com.mobile.micasaestucasa.data.dto.property.PropertyDto
import com.mobile.micasaestucasa.domain.model.property.Property
import org.junit.Assert.assertEquals
import org.junit.Test

class PropertyMapperTest {

    @Test
    fun `toDomain con campi null applica default coerenti`() {
        val dto = PropertyDto()

        val result = dto.toDomain()

        assertEquals("", result.id)
        assertEquals("", result.ownerId)
        assertEquals("", result.title)
        assertEquals(0.0, result.pricePerDay, 0.0)
        assertEquals(1, result.capacity)
        assertEquals(emptyList<String>(), result.keywords)
        assertEquals("", result.availableFrom)
        assertEquals("", result.availableTo)
    }

    @Test
    fun `toDto mantiene tutti i valori del domain model`() {
        val property = Property(
            id = "p1",
            ownerId = "owner1",
            title = "Loft",
            description = "Centro",
            latitude = 45.0,
            longitude = 9.0,
            city = "Milano",
            pricePerDay = 150.0,
            capacity = 4,
            keywords = listOf("wifi", "balcone"),
            imageUrls = listOf("img1"),
            availableFrom = "2026-07-01",
            availableTo = "2026-07-31",
            rating = 4.5,
            reviewsCount = 10
        )

        val dto = property.toDto()

        assertEquals("p1", dto.id)
        assertEquals("owner1", dto.ownerId)
        assertEquals("Loft", dto.title)
        assertEquals(150.0, dto.pricePerDay)
        assertEquals(4, dto.capacity)
        assertEquals(listOf("wifi", "balcone"), dto.keywords)
        assertEquals("2026-07-01", dto.availableFrom)
        assertEquals("2026-07-31", dto.availableTo)
    }
}
