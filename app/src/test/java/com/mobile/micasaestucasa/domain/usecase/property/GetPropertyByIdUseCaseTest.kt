package com.mobile.micasaestucasa.domain.usecase.property

import com.mobile.micasaestucasa.domain.model.property.Property
import com.mobile.micasaestucasa.domain.repository.property.PropertyRepo
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetPropertyByIdUseCaseTest {

    private lateinit var propertyRepo: PropertyRepo
    private lateinit var useCase: GetPropertyByIdUseCase

    private val sampleProperty = Property(
        id = "prop-1",
        ownerId = "owner-1",
        title = "Villa Santorini",
        description = "Beautiful villa perched on the caldera",
        latitude = 36.4161,
        longitude = 25.4322,
        city = "Santorini, Greece",
        pricePerDay = 450.0,
        capacity = 6,
        keywords = listOf("wifi", "pool", "parking"),
        imageUrls = listOf("https://example.com/img1.jpg"),
        availableFrom = "2026-06-01",
        availableTo = "2026-09-30",
        rating = 4.95,
        reviewsCount = 128
    )

    @Before
    fun setUp() {
        propertyRepo = mockk()
        useCase = GetPropertyByIdUseCase(propertyRepo)
    }

    @Test
    fun `propertyId vuoto ritorna failure e non chiama il repository`() = runTest {
        val result = useCase("")

        assertTrue(result.isFailure)
        assertEquals("ID proprietà obbligatorio", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { propertyRepo.getPropertyById(any()) }
    }

    @Test
    fun `propertyId blank ritorna failure e non chiama il repository`() = runTest {
        val result = useCase("   ")

        assertTrue(result.isFailure)
        assertEquals("ID proprietà obbligatorio", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { propertyRepo.getPropertyById(any()) }
    }

    @Test
    fun `propertyId valido ritorna la proprietà dal repository`() = runTest {
        coEvery { propertyRepo.getPropertyById("prop-1") } returns Result.success(sampleProperty)

        val result = useCase("prop-1")

        assertTrue(result.isSuccess)
        val property = result.getOrNull()
        assertEquals("prop-1", property?.id)
        assertEquals("Villa Santorini", property?.title)
        assertEquals(450.0, property?.pricePerDay ?: 0.0, 0.01)
        assertEquals("Santorini, Greece", property?.city)
        coVerify(exactly = 1) { propertyRepo.getPropertyById("prop-1") }
    }

    @Test
    fun `errore del repository viene propagato senza mascheramento`() = runTest {
        coEvery { propertyRepo.getPropertyById("prop-1") } returns Result.failure(
            Exception("Proprietà non trovata")
        )

        val result = useCase("prop-1")

        assertTrue(result.isFailure)
        assertEquals("Proprietà non trovata", result.exceptionOrNull()?.message)
        coVerify(exactly = 1) { propertyRepo.getPropertyById("prop-1") }
    }

    @Test
    fun `errore generico Firestore viene propagato`() = runTest {
        coEvery { propertyRepo.getPropertyById("prop-x") } returns Result.failure(
            Exception("Firestore offline")
        )

        val result = useCase("prop-x")

        assertTrue(result.isFailure)
        assertEquals("Firestore offline", result.exceptionOrNull()?.message)
        coVerify(exactly = 1) { propertyRepo.getPropertyById("prop-x") }
    }
}
