package com.mobile.micasaestucasa.ui.viewmodels.home

import app.cash.turbine.test
import com.mobile.micasaestucasa.domain.model.property.Property
import com.mobile.micasaestucasa.domain.repository.property.PropertyRepo
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private lateinit var viewModel: HomeViewModel
    private val propertyRepo: PropertyRepo = mockk()
    private val testDispatcher = UnconfinedTestDispatcher()

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

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        
        // Mock default behavior for init
        coEvery { 
            propertyRepo.searchProperties(any(), any(), any(), any(), any()) 
        } returns Result.success(mockProperties)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadHomeData updates state to Success when repository returns data`() = runTest {
        viewModel = HomeViewModel(propertyRepo)

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertEquals(mockProperties, state.properties)
            assertEquals(5, state.categories.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadHomeData updates state to Error when repository fails`() = runTest {
        val errorMessage = "Network Error"
        coEvery { 
            propertyRepo.searchProperties(any(), any(), any(), any(), any()) 
        } returns Result.failure(Exception(errorMessage))

        viewModel = HomeViewModel(propertyRepo)

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertEquals(errorMessage, state.error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onSearchQueryChanged triggers search when query is long enough`() = runTest {
        viewModel = HomeViewModel(propertyRepo)
        
        val searchResult = listOf(mockProperties[0].copy(title = "Roma Central"))
        coEvery { 
            propertyRepo.searchProperties("Roma", any(), any(), any(), any()) 
        } returns Result.success(searchResult)

        viewModel.onSearchQueryChanged("Roma")

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
        
        viewModel.onSearchQueryChanged("Ro")

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("Ro", state.searchQuery)
            // Should still have the initial mockProperties from init
            assertEquals(mockProperties, state.properties)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
