package com.mobile.micasaestucasa.ui.viewmodels.search

import app.cash.turbine.test
import com.mobile.micasaestucasa.domain.model.property.Property
import com.mobile.micasaestucasa.domain.model.search.SearchQuery
import com.mobile.micasaestucasa.domain.model.search.SearchResult
import com.mobile.micasaestucasa.domain.model.search.SearchSortOrder
import com.mobile.micasaestucasa.domain.usecase.search.SearchAvaliblePropertiesUseCase
import com.mobile.micasaestucasa.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val searchUseCase: SearchAvaliblePropertiesUseCase = mockk()
    private lateinit var viewModel: SearchViewModel

    private fun makeProperty(id: String, pricePerDay: Double = 100.0, rating: Double = 4.5) = Property(
        id = id,
        ownerId = "owner-1",
        title = "Casa $id",
        description = "Desc",
        latitude = 41.9,
        longitude = 12.5,
        city = "Roma",
        pricePerDay = pricePerDay,
        capacity = 2,
        keywords = listOf("wifi", "piscina"),
        imageUrls = emptyList(),
        availableFrom = "2026-07-01",
        availableTo = "2026-09-30",
        rating = rating,
        reviewsCount = 10
    )

    private fun makeResult(id: String, pricePerDay: Double = 100.0, rating: Double = 4.5) = SearchResult(
        property = makeProperty(id, pricePerDay, rating),
        totalPrice = pricePerDay * 7,
        nights = 7,
        isAvalible = true
    )

    private val validCity = "Roma"
    private val validStart = "2026-07-01"
    private val validEnd = "2026-07-08"
    private val validGuests = 2

    @Before
    fun setUp() {
        viewModel = SearchViewModel(searchUseCase)
    }

    // ── Initial state ────────────────────────────────────────────────────

    @Test
    fun `initial state is Idle`() = runTest {
        viewModel.uiState.test {
            assertEquals(SearchUiState.Idle, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `selectedPropertyId initial value is null`() = runTest {
        viewModel.selectedPropertyId.test {
            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── search() ─────────────────────────────────────────────────────────

    @Test
    fun `search() passa da Loading a Results con risultati corretti`() = runTest {
        val results = listOf(makeResult("p1"), makeResult("p2"))
        coEvery { searchUseCase(any()) } returns Result.success(results)

        viewModel.uiState.test {
            assertEquals(SearchUiState.Idle, awaitItem())

            viewModel.search(validCity, validStart, validEnd, validGuests)

            assertEquals(SearchUiState.Loading, awaitItem())
            val state = awaitItem()
            assertTrue(state is SearchUiState.Results)
            assertEquals(2, (state as SearchUiState.Results).results.size)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `search() con lista vuota emette Empty`() = runTest {
        coEvery { searchUseCase(any()) } returns Result.success(emptyList())

        viewModel.search(validCity, validStart, validEnd, validGuests)
        advanceUntilIdle()

        viewModel.uiState.test {
            assertEquals(SearchUiState.Empty, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `search() con errore emette Error con messaggio corretto`() = runTest {
        coEvery { searchUseCase(any()) } returns Result.failure(Exception("Connessione assente"))

        viewModel.search(validCity, validStart, validEnd, validGuests)
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is SearchUiState.Error)
            assertEquals("Connessione assente", (state as SearchUiState.Error).message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `search() con eccezione senza messaggio usa messaggio di default`() = runTest {
        coEvery { searchUseCase(any()) } returns Result.failure(Exception())

        viewModel.search(validCity, validStart, validEnd, validGuests)
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is SearchUiState.Error)
            assertEquals("Errore nella ricerca", (state as SearchUiState.Error).message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `search() passa la query corretta al use case`() = runTest {
        val keywords = listOf("wifi", "piscina")
        coEvery { searchUseCase(any()) } returns Result.success(listOf(makeResult("p1")))

        viewModel.search(validCity, validStart, validEnd, validGuests, keywords)
        advanceUntilIdle()

        coVerify(exactly = 1) {
            searchUseCase(
                SearchQuery(
                    city = validCity,
                    startDate = validStart,
                    endDate = validEnd,
                    guestsCount = validGuests,
                    keywords = keywords
                )
            )
        }
    }

    // ── applySortOrder() ─────────────────────────────────────────────────

    @Test
    fun `applySortOrder non fa nulla se non c'è una query corrente`() = runTest {
        viewModel.applySortOrder(SearchSortOrder.PRICE_ASC)
        advanceUntilIdle()

        coVerify(exactly = 0) { searchUseCase(any()) }

        viewModel.uiState.test {
            assertEquals(SearchUiState.Idle, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `applySortOrder applica nuovo ordinamento alla query corrente`() = runTest {
        val results = listOf(makeResult("p1"))
        coEvery { searchUseCase(any()) } returns Result.success(results)

        // Prima eseguo una ricerca per impostare currentQuery
        viewModel.search(validCity, validStart, validEnd, validGuests)
        advanceUntilIdle()

        // Poi cambio l'ordinamento
        viewModel.applySortOrder(SearchSortOrder.PRICE_DESC)
        advanceUntilIdle()

        // Deve essere stato chiamato 2 volte: una per search e una per applySortOrder
        coVerify(exactly = 2) { searchUseCase(any()) }
    }

    // ── applyMaxPrice() ──────────────────────────────────────────────────

    @Test
    fun `applyMaxPrice non fa nulla se non c'è una query corrente`() = runTest {
        viewModel.applyMaxPrice(200.0)
        advanceUntilIdle()

        coVerify(exactly = 0) { searchUseCase(any()) }
    }

    @Test
    fun `applyMaxPrice aggiorna la query con il prezzo massimo`() = runTest {
        val results = listOf(makeResult("p1"))
        coEvery { searchUseCase(any()) } returns Result.success(results)

        viewModel.search(validCity, validStart, validEnd, validGuests)
        advanceUntilIdle()

        viewModel.applyMaxPrice(150.0)
        advanceUntilIdle()

        coVerify(exactly = 1) {
            searchUseCase(match { it.maxPricePerDay == 150.0 })
        }
    }

    @Test
    fun `applyMaxPrice con null rimuove il filtro prezzo`() = runTest {
        val results = listOf(makeResult("p1"))
        coEvery { searchUseCase(any()) } returns Result.success(results)

        // Sequenza: search (maxPrice=null) → applyMaxPrice(100.0) → applyMaxPrice(null)
        viewModel.search(validCity, validStart, validEnd, validGuests)
        advanceUntilIdle()
        viewModel.applyMaxPrice(100.0)
        advanceUntilIdle()
        viewModel.applyMaxPrice(null)
        advanceUntilIdle()

        // La search iniziale + applyMaxPrice(null) producono 2 query con maxPricePerDay=null
        coVerify(exactly = 2) {
            searchUseCase(match { it.maxPricePerDay == null })
        }
        // Solo applyMaxPrice(100.0) produce una query con maxPricePerDay=100.0
        coVerify(exactly = 1) {
            searchUseCase(match { it.maxPricePerDay == 100.0 })
        }
    }

    // ── applyCategories() ────────────────────────────────────────────────

    @Test
    fun `applyCategories non fa nulla senza query corrente`() = runTest {
        viewModel.applyCategories(listOf("piscina"))
        advanceUntilIdle()

        coVerify(exactly = 0) { searchUseCase(any()) }
    }

    @Test
    fun `applyCategories aggiunge le categorie alle keywords della query`() = runTest {
        val results = listOf(makeResult("p1"))
        coEvery { searchUseCase(any()) } returns Result.success(results)

        viewModel.search(validCity, validStart, validEnd, validGuests)
        advanceUntilIdle()

        viewModel.applyCategories(listOf("piscina", "vista"))
        advanceUntilIdle()

        coVerify(exactly = 1) {
            searchUseCase(
                match { query ->
                    query.keywords.containsAll(listOf("piscina", "vista"))
                }
            )
        }
    }

    // ── selectProperty() ─────────────────────────────────────────────────

    @Test
    fun `selectProperty aggiorna selectedPropertyId`() = runTest {
        viewModel.selectProperty("prop-42")
        advanceUntilIdle()

        viewModel.selectedPropertyId.test {
            assertEquals("prop-42", awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `selectProperty con null deseleziona la proprietà`() = runTest {
        viewModel.selectProperty("prop-42")
        advanceUntilIdle()
        viewModel.selectProperty(null)
        advanceUntilIdle()

        viewModel.selectedPropertyId.test {
            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── reset() ───────────────────────────────────────────────────────────

    @Test
    fun `reset riporta lo stato a Idle e pulisce selectedPropertyId`() = runTest {
        val results = listOf(makeResult("p1"))
        coEvery { searchUseCase(any()) } returns Result.success(results)

        viewModel.search(validCity, validStart, validEnd, validGuests)
        advanceUntilIdle()
        viewModel.selectProperty("p1")

        viewModel.reset()

        viewModel.uiState.test {
            assertEquals(SearchUiState.Idle, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        viewModel.selectedPropertyId.test {
            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `reset impedisce applySortOrder di fare la chiamata`() = runTest {
        val results = listOf(makeResult("p1"))
        coEvery { searchUseCase(any()) } returns Result.success(results)

        viewModel.search(validCity, validStart, validEnd, validGuests)
        advanceUntilIdle()
        viewModel.reset()

        // Dopo reset currentQuery è null → applySortOrder non deve chiamare il use case
        viewModel.applySortOrder(SearchSortOrder.PRICE_ASC)
        advanceUntilIdle()

        // Solo 1 chiamata: quella originale di search
        coVerify(exactly = 1) { searchUseCase(any()) }
    }
}
