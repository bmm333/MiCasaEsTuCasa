package com.mobile.micasaestucasa.domain.usecase

import com.mobile.micasaestucasa.domain.model.property.Property
import com.mobile.micasaestucasa.domain.repository.property.PropertyRepo
import com.mobile.micasaestucasa.domain.usecase.property.SearchPropertiesUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SearchPropertiesUseCaseTest {

    private lateinit var propertyRepository: PropertyRepo
    private lateinit var searchPropertiesUseCase: SearchPropertiesUseCase

    private val mockProperty = Property(
        id = "prop1",
        ownerId = "owner1",
        title = "Casa Torino",
        description = "desc",
        latitude = 45.07,
        longitude = 7.68,
        city = "Torino",
        pricePerDay = 80.0,
        capacity = 2,
        keywords = listOf("wifi"),
        imageUrls = emptyList(),
        availableFrom = "2026-06-01",
        availableTo = "2026-08-31"
    )

    @Before
    fun setUp() {
        propertyRepository = mockk()
        searchPropertiesUseCase = SearchPropertiesUseCase(propertyRepository)
    }

    @Test
    fun `ricerca con parametri validi ritorna lista`() = runTest {
        coEvery {
            propertyRepository.searchProperties(any(), any(), any(), any(), any())
        } returns Result.success(listOf(mockProperty))

        val result = searchPropertiesUseCase("Torino", "2026-07-01", "2026-07-10", 2)

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
    }

    @Test
    fun `ricerca con city vuota ritorna failure`() = runTest {
        val result = searchPropertiesUseCase("", "2026-07-01", "2026-07-10", 2)

        assertTrue(result.isFailure)
        assertEquals("Citta Obbligatoria", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { propertyRepository.searchProperties(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `ricerca con capacity zero ritorna failure`() = runTest {
        val result = searchPropertiesUseCase("Torino", "2026-07-01", "2026-07-10", 0)

        assertTrue(result.isFailure)
        coVerify(exactly = 0) { propertyRepository.searchProperties(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `ricerca con startDate non valida ritorna failure`() = runTest {
        val result = searchPropertiesUseCase("Torino", "data-non-valida", "2026-07-10", 2)

        assertTrue(result.isFailure)
        assertEquals("Start date non valida", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { propertyRepository.searchProperties(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `ricerca con endDate non valida ritorna failure`() = runTest {
        val result = searchPropertiesUseCase("Torino", "2026-07-01", "data-non-valida", 2)

        assertTrue(result.isFailure)
        assertEquals("End date non valida", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { propertyRepository.searchProperties(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `ricerca con startDate dopo endDate ritorna failure`() = runTest {
        val result = searchPropertiesUseCase("Torino", "2026-07-10", "2026-07-01", 2)

        assertTrue(result.isFailure)
        assertEquals(
            "Start date deve essere precedente o uguale a end date",
            result.exceptionOrNull()?.message
        )
        coVerify(exactly = 0) { propertyRepository.searchProperties(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `ricerca con keywords ritorna lista filtrata`() = runTest {
        coEvery {
            propertyRepository.searchProperties(any(), any(), any(), any(), any())
        } returns Result.success(listOf(mockProperty))

        val result = searchPropertiesUseCase(
            "Torino",
            "2026-07-01",
            "2026-07-10",
            2,
            keywords = listOf("wifi")
        )

        assertTrue(result.isSuccess)
    }

    @Test
    fun `ricerca ritorna lista vuota se nessun risultato`() = runTest {
        coEvery {
            propertyRepository.searchProperties(any(), any(), any(), any(), any())
        } returns Result.success(emptyList())

        val result = searchPropertiesUseCase("Milano", "2026-07-01", "2026-07-10", 1)

        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()?.size)
    }

    @Test
    fun `ricerca propaga errore Firestore`() = runTest {
        coEvery {
            propertyRepository.searchProperties(any(), any(), any(), any(), any())
        } returns Result.failure(Exception("Network error"))

        val result = searchPropertiesUseCase("Torino", "2026-07-01", "2026-07-10", 2)

        assertTrue(result.isFailure)
    }
}
