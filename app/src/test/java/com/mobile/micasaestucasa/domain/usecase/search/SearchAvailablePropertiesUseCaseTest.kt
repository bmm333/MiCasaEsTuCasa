package com.mobile.micasaestucasa.domain.usecase.search

import com.mobile.micasaestucasa.domain.model.property.Property
import com.mobile.micasaestucasa.domain.model.search.SearchQuery
import com.mobile.micasaestucasa.domain.repository.booking.BookingRepo
import com.mobile.micasaestucasa.domain.repository.property.PropertyRepo
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class SearchAvailablePropertiesUseCaseTest{
    private lateinit var propertyRepository: PropertyRepo
    private lateinit var bookingRepository: BookingRepo
    private lateinit var useCase: SearchAvaliblePropertiesUseCase

    private val validQuery = SearchQuery(
        city = "Milano",
        startDate = "2026-07-01",
        endDate = "2026-07-10",
        guestsCount = 2
    )

    private fun makeProperty(
        id: String,
        city: String = "Milano",
        pricePerDay: Double = 100.0,
        capacity: Int = 2,
        rating: Double = 4.5,
        keywords: List<String> = listOf("wifi")
    ) = Property(
        id = id,
        ownerId = "owner1",
        title = "Casa $id",
        description = "desc",
        latitude = 45.46,
        longitude = 9.19,
        city = city,
        pricePerDay = pricePerDay,
        capacity = capacity,
        keywords = keywords,
        imageUrls = emptyList(),
        availableFrom = "2026-01-01",
        availableTo = "2026-12-31",
        rating = rating,
        reviewsCount = 5
    )
    @Before
    fun setUp()
    {
        propertyRepository= mockk()
        bookingRepository= mockk()
        useCase= SearchAvaliblePropertiesUseCase(propertyRepository, bookingRepository)
    }
    @Test
    fun `valid query returns properties that are avalible with total price` () = runTest {
        val props = listOf(makeProperty("p1"), makeProperty("p2"))
        coEvery { propertyRepository.searchProperties(any(), any(), any(), any(), any()) } returns
                Result.success(props)
        coEvery { bookingRepository.hasOverlappingBooking(any(), any(), any()) } returns
                Result.success(false)
        val result = useCase(validQuery)
        assertTrue(result.isSuccess)
        val results = result.getOrThrow()
        assertEquals(2, results.size)
        assertEquals(900.0, results[0].totalPrice, 0.01)
        assertEquals(9, results[0].nights)
        assertTrue(results[0].isAvalible)
    }
    @Test
    fun `property with active bookings is excluded from the results`()=runTest {
        val props = listOf(makeProperty("p1"), makeProperty("p2"))
        coEvery { propertyRepository.searchProperties(any(), any(), any(), any(), any()) } returns
                Result.success(props)
        // p1 disponibile, p2 occupata
        coEvery { bookingRepository.hasOverlappingBooking("p1", any(), any()) } returns
                Result.success(false)
        coEvery { bookingRepository.hasOverlappingBooking("p2", any(), any()) } returns
                Result.success(true)
        val result = useCase(validQuery)
        assertTrue(result.isSuccess)
        val results = result.getOrThrow()
        assertEquals(1, results.size)
        assertEquals("p1", results[0].property.id)
    }
    @Test
    fun `all properties occupied return empty list`() = runTest {
        val props = listOf(makeProperty("p1"), makeProperty("p2"), makeProperty("p3"))
        coEvery { propertyRepository.searchProperties(any(), any(), any(), any(), any()) } returns
                Result.success(props)
        coEvery { bookingRepository.hasOverlappingBooking(any(), any(), any()) } returns
                Result.success(true)

        val result = useCase(validQuery)

        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow().isEmpty())
    }
    @Test
    fun `empty firestore returns empty list`() = runTest {
        coEvery { propertyRepository.searchProperties(any(), any(), any(), any(), any()) } returns
                Result.success(emptyList())

        val result = useCase(validQuery)

        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow().isEmpty())
        coVerify(exactly = 0) { bookingRepository.hasOverlappingBooking(any(), any(), any()) }
    }
    @Test
    fun `city empty returns failure`() = runTest {
        val result = useCase(validQuery.copy(city = ""))

        assertTrue(result.isFailure)
        assertEquals("City is required", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { propertyRepository.searchProperties(any(), any(), any(), any(), any()) }
    }
    @Test
    fun `guestsCount zero returns failure`() = runTest {
        val result = useCase(validQuery.copy(guestsCount = 0))

        assertTrue(result.isFailure)
        assertEquals("Guest count must be at least 1", result.exceptionOrNull()?.message)
    }
    @Test
    fun `startDate equal to endDate returns failure`() = runTest {
        val result = useCase(validQuery.copy(startDate = "2026-07-10", endDate = "2026-07-10"))

        assertTrue(result.isFailure)
        assertEquals("Start date must be before end date", result.exceptionOrNull()?.message)
    }

    @Test
    fun `startDate after endDate returns failure`() = runTest {
        val result = useCase(validQuery.copy(startDate = "2026-07-15", endDate = "2026-07-10"))

        assertTrue(result.isFailure)
    }
    @Test
    fun `maxPricePerDay excludes properties`() = runTest {
        val props = listOf(
            makeProperty("cheap", pricePerDay = 50.0),
            makeProperty("expensive", pricePerDay = 200.0)
        )
        coEvery { propertyRepository.searchProperties(any(), any(), any(), any(), any()) } returns
                Result.success(props)
        coEvery { bookingRepository.hasOverlappingBooking(any(), any(), any()) } returns
                Result.success(false)
        val result = useCase(validQuery.copy(maxPricePerDay = 100.0))
        assertTrue(result.isSuccess)
        val results = result.getOrThrow()
        assertEquals(1, results.size)
        assertEquals("cheap", results[0].property.id)
    }

}