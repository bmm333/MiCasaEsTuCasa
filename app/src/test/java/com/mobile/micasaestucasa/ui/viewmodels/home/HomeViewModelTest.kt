package com.mobile.micasaestucasa.ui.viewmodels.home

import app.cash.turbine.test
import com.mobile.micasaestucasa.domain.model.property.Property
import com.mobile.micasaestucasa.domain.repository.property.PropertyRepo
import com.mobile.micasaestucasa.domain.repository.whishlist.WhishlistRepo
import com.mobile.micasaestucasa.domain.util.Resource
import com.mobile.micasaestucasa.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: HomeViewModel
    private val propertyRepo: PropertyRepo = mockk()
    private val wishlistRepo: WhishlistRepo = mockk(relaxed = true)

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
        viewModel = HomeViewModel(propertyRepo, wishlistRepo)
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
        viewModel = HomeViewModel(propertyRepo, wishlistRepo)
        advanceUntilIdle()

        val searchResult = listOf(mockProperties[0].copy(title = "Roma Central"))
        coEvery {
            propertyRepo.searchProperties("Roma", any(), any(), any(), any())
        } returns Result.success(searchResult)

        // performSearch uses Dispatchers.IO, so the state update is asynchronous.
        // Turbine's awaitItem() waits with a timeout for each emission, which lets
        // us reliably observe updates even from real IO threads.
        viewModel.uiState.test {
            // Consume stable initial state before triggering the action
            awaitItem()

            viewModel.onSearchQueryChanged("Roma")

            // The synchronous update emits searchQuery="Roma" but properties=mockProperties.
            // The IO coroutine later emits properties=searchResult.
            // Loop until we find the final state with the expected properties.
            var state = awaitItem()
            while (state.properties != searchResult || state.isLoading) {
                state = awaitItem()
            }

            assertEquals("Roma", state.searchQuery)
            assertEquals(searchResult, state.properties)
            cancelAndIgnoreRemainingEvents()
        }
    }


    @Test
    fun `onSearchQueryChanged does not trigger search when query is too short`() = runTest {
        viewModel = HomeViewModel(propertyRepo, wishlistRepo)
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

    @Test
    fun `onSearchQueryChanged with empty string restores home data`() = runTest {
        viewModel = HomeViewModel(propertyRepo, wishlistRepo)
        advanceUntilIdle()

        // Prima esegui una ricerca
        viewModel.onSearchQueryChanged("Roma")
        advanceUntilIdle()

        // Poi cancella la query
        viewModel.onSearchQueryChanged("")
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("", state.searchQuery)
            assertEquals(mockProperties, state.properties)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── toggleSaved ─────────────────────────────────────────────────────

    @Test
    fun `toggleSaved adds propertyId to savedPropertyIds when isSaved is true`() = runTest {
        coEvery { wishlistRepo.toggleSavedProperty("user-1", "prop-1") } returns Result.success(true)

        viewModel = HomeViewModel(propertyRepo, wishlistRepo)
        advanceUntilIdle()

        viewModel.toggleSaved("user-1", "prop-1")
        advanceUntilIdle()

        assertTrue(viewModel.savedPropertyIds.value.contains("prop-1"))
    }

    @Test
    fun `toggleSaved removes propertyId from savedPropertyIds when isSaved is false`() = runTest {
        // Prima aggiunge
        coEvery { wishlistRepo.toggleSavedProperty("user-1", "prop-1") } returns Result.success(true)
        viewModel = HomeViewModel(propertyRepo, wishlistRepo)
        advanceUntilIdle()
        viewModel.toggleSaved("user-1", "prop-1")
        advanceUntilIdle()
        assertTrue(viewModel.savedPropertyIds.value.contains("prop-1"))

        // Poi rimuove
        coEvery { wishlistRepo.toggleSavedProperty("user-1", "prop-1") } returns Result.success(false)
        viewModel.toggleSaved("user-1", "prop-1")
        advanceUntilIdle()

        assertFalse(viewModel.savedPropertyIds.value.contains("prop-1"))
    }

    @Test
    fun `toggleSaved does not update state when repo fails`() = runTest {
        coEvery { wishlistRepo.toggleSavedProperty("user-1", "prop-1") } returns Result.failure(Exception("Network error"))

        viewModel = HomeViewModel(propertyRepo, wishlistRepo)
        advanceUntilIdle()

        viewModel.toggleSaved("user-1", "prop-1")
        advanceUntilIdle()

        assertFalse(viewModel.savedPropertyIds.value.contains("prop-1"))
    }

    // ── loadSavedIds ────────────────────────────────────────────────────

    @Test
    fun `loadSavedIds populates savedPropertyIds from repository`() = runTest {
        val savedIds = setOf("prop-1", "prop-2", "prop-3")
        coEvery { wishlistRepo.getSavedPropertyIds("user-1") } returns Result.success(savedIds)

        viewModel = HomeViewModel(propertyRepo, wishlistRepo)
        advanceUntilIdle()

        // Use Turbine to wait for the savedPropertyIds update from Dispatchers.IO coroutine
        viewModel.savedPropertyIds.test {
            awaitItem() // consume initial empty set emitted by init
            viewModel.loadSavedIds("user-1")
            val ids = awaitItem()
            assertEquals(savedIds, ids)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadSavedIds with blank userId does nothing`() = runTest {
        viewModel = HomeViewModel(propertyRepo, wishlistRepo)
        advanceUntilIdle()

        viewModel.loadSavedIds("")
        advanceUntilIdle()

        coVerify(exactly = 0) { wishlistRepo.getSavedPropertyIds(any()) }
        assertTrue(viewModel.savedPropertyIds.value.isEmpty())
    }

    @Test
    fun `loadSavedIds does not update state on repository failure`() = runTest {
        coEvery { wishlistRepo.getSavedPropertyIds("user-1") } returns Result.failure(Exception("Offline"))

        viewModel = HomeViewModel(propertyRepo, wishlistRepo)
        advanceUntilIdle()

        viewModel.loadSavedIds("user-1")
        advanceUntilIdle()

        assertTrue(viewModel.savedPropertyIds.value.isEmpty())
    }
}
