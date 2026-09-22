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

class GetOwnerPropertiesUseCaseTest {

    private lateinit var propertyRepo: PropertyRepo
    private lateinit var useCase: GetOwnerPropertiesUseCase

    private val sampleProperty = Property(
        id = "prop-1",
        ownerId = "owner-1",
        title = "Casa vista mare",
        description = "descrizione",
        latitude = 41.9,
        longitude = 12.5,
        city = "Roma",
        pricePerDay = 120.0,
        capacity = 3,
        keywords = listOf("wifi"),
        imageUrls = emptyList(),
        availableFrom = "2026-07-01",
        availableTo = "2026-08-01"
    )

    @Before
    fun setUp() {
        propertyRepo = mockk()
        useCase = GetOwnerPropertiesUseCase(propertyRepo)
    }

    @Test
    fun `ownerId vuoto ritorna failure e non chiama il repository`() = runTest {
        val result = useCase("")

        assertTrue(result.isFailure)
        assertEquals("Id Proprietario obbligatorio", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { propertyRepo.getPropertiesByOwner(any()) }
    }

    @Test
    fun `ownerId valido propaga lista dal repository`() = runTest {
        coEvery { propertyRepo.getPropertiesByOwner("owner-1") } returns Result.success(listOf(sampleProperty))

        val result = useCase("owner-1")

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        assertEquals("prop-1", result.getOrNull()?.first()?.id)
        coVerify(exactly = 1) { propertyRepo.getPropertiesByOwner("owner-1") }
    }

    @Test
    fun `errore del repository viene propagato senza mascheramento`() = runTest {
        coEvery { propertyRepo.getPropertiesByOwner("owner-1") } returns Result.failure(Exception("Firestore offline"))

        val result = useCase("owner-1")

        assertTrue(result.isFailure)
        assertEquals("Firestore offline", result.exceptionOrNull()?.message)
        coVerify(exactly = 1) { propertyRepo.getPropertiesByOwner("owner-1") }
    }
}
