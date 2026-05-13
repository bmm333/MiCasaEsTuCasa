package com.mobile.micasaestucasa.ui.viewmodels.home

import app.cash.turbine.test
import com.mobile.micasaestucasa.domain.model.property.Property
import com.mobile.micasaestucasa.domain.repository.property.PropertyRepo
import com.mobile.micasaestucasa.domain.util.Resource
import com.mobile.micasaestucasa.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: HomeViewModel
    private val propertyRepo: PropertyRepo = mockk()

    private val mockProperties = listOf(
        Property(
            id = "1",
            ownerId = "owner1",
            title = "Villa Paradiso",
            description = "Bella villa",
            latitude = 0.0,
            longitude = 0.0,
            city = "Roma",
            pricePerDay = 100.0,
            capacity = 4,
            keywords = emptyList(),
            imageUrls = listOf("url1"),
            availableFrom = "2024-01-01",
            availableTo = "2024-12-31",
            rating = 4.5,
            reviewsCount = 10
        )
    )

    private val mockCategories = listOf(
        Category("Modern", "holiday_village"),
        Category("Rustic", "cabin")
    )

    @Before
    fun setUp() {
        // Mock default behavior for init
        coEvery { propertyRepo.getCategories() } returns Result.success(mockCategories)
        every { propertyRepo.getAllPropertiesFlow() } returns flowOf(Resource.Success(mockProperties))
        coEvery {
            propertyRepo.searchProperties(any(), any(), any(), any(), any())
        } returns Result.success(mockProperties)
    }

    @Test
    fun `loadHomeData updates state to Success when repository returns data`() = runTest {
        viewModel = HomeViewModel(propertyRepo)
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertEquals(mockProperties, state.properties)
            assertEquals(mockCategories, state.categories)
            cancelAndIgnoreRemainingEvents()
        }
    }



    @Test
    fun `onSearchQueryChanged triggers search when query is long enough`() = runTest {
        viewModel = HomeViewModel(propertyRepo)
        advanceUntilIdle()

        val searchResult = listOf(mockProperties[0].copy(title = "Roma Central"))
        coEvery {
            propertyRepo.searchProperties("Roma", any(), any(), any(), any())
        } returns Result.success(searchResult)

        viewModel.onSearchQueryChanged("Roma")
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("Roma", state.searchQuery)
            assertEquals(searchResult, state.properties)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onSearchQueryChanged does not trigger search when query is too short`() = runTest {
        viewModel = HomeViewModel(propertyRepo)
        advanceUntilIdle()

        viewModel.onSearchQueryChanged("Ro")
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("Ro", state.searchQuery)
            // Should still have the initial mockProperties from init
            assertEquals(mockProperties, state.properties)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
