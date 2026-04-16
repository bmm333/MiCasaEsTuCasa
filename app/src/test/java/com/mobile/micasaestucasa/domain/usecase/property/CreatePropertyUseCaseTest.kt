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

class CreatePropertyUseCaseTest {
    private lateinit var propertyRepository: PropertyRepo
    private lateinit var createPropertyUseCase: CreatePropertyUseCase

    private val validProperty = Property(
        id = "",
        ownerId = "owner123",
        title = "Appartamento Milano Centro",
        description = "Bellissimoissimo appartamento",
        latitude = 45.464664,
        longitude = 9.188540,
        city = "Milano",
        pricePerDay = 120.0,
        capacity = 4,
        keywords = listOf("wifi", "piscina"),
        imageUrls = emptyList(),
        availableFrom = "2026-06-01",
        availableTo = "2026-08-31"
    )

    @Before
    fun setUp() {
        propertyRepository = mockk()
        createPropertyUseCase = CreatePropertyUseCase(propertyRepository)
    }

    @Test
    fun `crea property con dati validi ritorna id`() = runTest {
        coEvery { propertyRepository.createProperty(any()) } returns Result.success("newId123")

        val result = createPropertyUseCase(validProperty)

        assertTrue(result.isSuccess)
        assertEquals("newId123", result.getOrNull())
    }

    @Test
    fun `crea property con titolo vuoto ritorna failure`() = runTest {
        val property = validProperty.copy(title = "")

        val result = createPropertyUseCase(property)

        assertTrue(result.isFailure)
        assertEquals("Titolo obbligatorio", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { propertyRepository.createProperty(any()) }
    }

    @Test
    fun `crea property con prezzo zero ritorna failure`() = runTest {
        val property = validProperty.copy(pricePerDay = 0.0)

        val result = createPropertyUseCase(property)

        assertTrue(result.isFailure)
        assertEquals("Prezzo deve essere maggiore di 0", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { propertyRepository.createProperty(any()) }
    }

    @Test
    fun `crea property con prezzo negativo ritorna failure`() = runTest {
        val property = validProperty.copy(pricePerDay = -50.0)

        val result = createPropertyUseCase(property)

        assertTrue(result.isFailure)
        coVerify(exactly = 0) { propertyRepository.createProperty(any()) }
    }

    @Test
    fun `crea property con capacity zero ritorna failure`() = runTest {
        val property = validProperty.copy(capacity = 0)

        val result = createPropertyUseCase(property)

        assertTrue(result.isFailure)
        coVerify(exactly = 0) { propertyRepository.createProperty(any()) }
    }

    @Test
    fun `crea property con city vuota ritorna failure`() = runTest {
        val property = validProperty.copy(city = "")

        val result = createPropertyUseCase(property)

        assertTrue(result.isFailure)
        assertEquals("Citta obbligatoria", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { propertyRepository.createProperty(any()) }
    }

    @Test
    fun `crea property propaga errore Firestore`() = runTest {
        coEvery { propertyRepository.createProperty(any()) } returns
            Result.failure(Exception("Firestore error"))

        val result = createPropertyUseCase(validProperty)

        assertTrue(result.isFailure)
        assertEquals("Firestore error", result.exceptionOrNull()?.message)
    }
}
