package com.mobile.micasaestucasa.ui.viewmodels.property

import app.cash.turbine.test
import com.mobile.micasaestucasa.domain.model.property.Property
import com.mobile.micasaestucasa.domain.model.user.User
import com.mobile.micasaestucasa.domain.repository.user.UserRepo
import com.mobile.micasaestucasa.domain.repository.whishlist.WhishlistRepo
import com.mobile.micasaestucasa.domain.usecase.admin.AddUserReportUseCase
import com.mobile.micasaestucasa.domain.usecase.property.CreatePropertyUseCase
import com.mobile.micasaestucasa.domain.usecase.property.DeletePropertyUseCase
import com.mobile.micasaestucasa.domain.usecase.property.DemoteHostUseCase
import com.mobile.micasaestucasa.domain.usecase.property.GetOwnerPropertiesUseCase
import com.mobile.micasaestucasa.domain.usecase.property.GetPropertyByIdUseCase
import com.mobile.micasaestucasa.domain.usecase.property.SearchPropertiesUseCase
import com.mobile.micasaestucasa.domain.usecase.review.GetPropertyReviewsUseCase
import com.mobile.micasaestucasa.domain.usecase.review.ReplyToReviewUseCase
import com.mobile.micasaestucasa.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PropertyViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: PropertyViewModel
    private val searchPropertiesUseCase: SearchPropertiesUseCase = mockk()
    private val getOwnerPropertiesUseCase: GetOwnerPropertiesUseCase = mockk()
    private val createPropertyUseCase: CreatePropertyUseCase = mockk()
    private val getPropertyByIdUseCase: GetPropertyByIdUseCase = mockk()
    private val addUserReportUseCase: AddUserReportUseCase = mockk(relaxed = true)
    private val wishlistRepo: WhishlistRepo = mockk(relaxed = true)
    private val userRepo: UserRepo = mockk(relaxed = true)
    private val deletePropertyUseCase: DeletePropertyUseCase = mockk(relaxed = true)
    private val demoteHostUseCase: DemoteHostUseCase = mockk(relaxed = true)
    private val getPropertyReviewsUseCase: GetPropertyReviewsUseCase = mockk(relaxed = true)
    private val replyToReviewUseCase: ReplyToReviewUseCase = mockk(relaxed = true)

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
        keywords = listOf("wifi", "pool"),
        imageUrls = listOf("https://example.com/img1.jpg"),
        availableFrom = "2026-06-01",
        availableTo = "2026-09-30",
        rating = 4.95,
        reviewsCount = 128
    )

    private val samplePropertyList = listOf(sampleProperty)

    @Before
    fun setUp() {
        viewModel = PropertyViewModel(
            searchPropertiesUseCase = searchPropertiesUseCase,
            getOwnerPropertiesUseCase = getOwnerPropertiesUseCase,
            createPropertyUseCase = createPropertyUseCase,
            getPropertyByIdUseCase = getPropertyByIdUseCase,
            addUserReportUseCase = addUserReportUseCase,
            wishlistRepo = wishlistRepo,
            userRepo = userRepo,
            deletePropertyUseCase = deletePropertyUseCase,
            demoteHostUseCase = demoteHostUseCase,
            getPropertyReviewsUseCase = getPropertyReviewsUseCase,
            replyToReviewUseCase = replyToReviewUseCase
        )
    }

    // ── loadPropertyDetail ──────────────────────────────────────────────

    @Test
    fun `loadPropertyDetail emette DetailSuccess con la proprietà corretta`() = runTest {
        coEvery { getPropertyByIdUseCase("prop-1") } returns Result.success(sampleProperty)
        coEvery { getPropertyReviewsUseCase("prop-1") } returns Result.success(emptyList())

        viewModel.loadPropertyDetail("prop-1")
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is PropertyUiState.DetailSuccess)
            val detail = state as PropertyUiState.DetailSuccess
            assertEquals("prop-1", detail.property.id)
            assertEquals("Villa Santorini", detail.property.title)
            assertEquals(450.0, detail.property.pricePerDay, 0.01)
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 1) { getPropertyByIdUseCase("prop-1") }
    }

    @Test
    fun `loadPropertyDetail emette Error quando il usecase fallisce`() = runTest {
        coEvery { getPropertyByIdUseCase("prop-x") } returns Result.failure(
            Exception("Proprietà non trovata")
        )

        viewModel.loadPropertyDetail("prop-x")
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is PropertyUiState.Error)
            assertEquals("Proprietà non trovata", (state as PropertyUiState.Error).message)
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 1) { getPropertyByIdUseCase("prop-x") }
    }

    @Test
    fun `loadPropertyDetail emette Error con messaggio di default per eccezione senza messaggio`() = runTest {
        coEvery { getPropertyByIdUseCase("prop-y") } returns Result.failure(
            Exception()
        )

        viewModel.loadPropertyDetail("prop-y")
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is PropertyUiState.Error)
            assertEquals("Errore caricamento proprietà", (state as PropertyUiState.Error).message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadPropertyDetail passa da Loading a DetailSuccess`() = runTest {
        coEvery { getPropertyByIdUseCase("prop-1") } returns Result.success(sampleProperty)
        coEvery { getPropertyReviewsUseCase("prop-1") } returns Result.success(emptyList())

        viewModel.uiState.test {
            // Stato iniziale Idle
            assertEquals(PropertyUiState.Idle, awaitItem())

            viewModel.loadPropertyDetail("prop-1")

            // Passa a Loading
            assertEquals(PropertyUiState.Loading, awaitItem())

            // Poi a DetailSuccess
            val success = awaitItem()
            assertTrue(success is PropertyUiState.DetailSuccess)
            assertEquals("prop-1", (success as PropertyUiState.DetailSuccess).property.id)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── resetState ──────────────────────────────────────────────────────

    @Test
    fun `resetState riporta lo stato a Idle`() = runTest {
        coEvery { getPropertyByIdUseCase("prop-1") } returns Result.success(sampleProperty)
        coEvery { getPropertyReviewsUseCase("prop-1") } returns Result.success(emptyList())

        viewModel.loadPropertyDetail("prop-1")
        advanceUntilIdle()

        // Verifica che sia in DetailSuccess
        viewModel.uiState.test {
            assertTrue(awaitItem() is PropertyUiState.DetailSuccess)
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.resetState()

        viewModel.uiState.test {
            assertEquals(PropertyUiState.Idle, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── searchProperties ────────────────────────────────────────────────

    @Test
    fun `searchProperties emette SearchSuccess con i risultati`() = runTest {
        coEvery {
            searchPropertiesUseCase("Roma", "2026-07-01", "2026-07-15", 2, emptyList())
        } returns Result.success(samplePropertyList)

        viewModel.searchProperties("Roma", "2026-07-01", "2026-07-15", 2)
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is PropertyUiState.SearchSuccess)
            assertEquals(1, (state as PropertyUiState.SearchSuccess).properties.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `searchProperties emette Error quando il usecase fallisce`() = runTest {
        coEvery {
            searchPropertiesUseCase("Roma", "2026-07-01", "2026-07-15", 2, emptyList())
        } returns Result.failure(Exception("Network error"))

        viewModel.searchProperties("Roma", "2026-07-01", "2026-07-15", 2)
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is PropertyUiState.Error)
            assertEquals("Network error", (state as PropertyUiState.Error).message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `searchProperties non ripete la chiamata se i parametri sono uguali`() = runTest {
        coEvery {
            searchPropertiesUseCase("Roma", "2026-07-01", "2026-07-15", 2, emptyList())
        } returns Result.success(samplePropertyList)

        viewModel.searchProperties("Roma", "2026-07-01", "2026-07-15", 2)
        advanceUntilIdle()

        // Seconda chiamata con stessi parametri – non dovrebbe richiamare il usecase
        viewModel.searchProperties("Roma", "2026-07-01", "2026-07-15", 2)
        advanceUntilIdle()

        coVerify(exactly = 1) {
            searchPropertiesUseCase("Roma", "2026-07-01", "2026-07-15", 2, emptyList())
        }
    }

    // ── loadOwnerProperties ─────────────────────────────────────────────

    @Test
    fun `loadOwnerProperties emette OwnerSuccess con le proprietà`() = runTest {
        coEvery { getOwnerPropertiesUseCase("owner-1") } returns Result.success(samplePropertyList)

        viewModel.loadOwnerProperties("owner-1")
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is PropertyUiState.OwnerSuccess)
            assertEquals(1, (state as PropertyUiState.OwnerSuccess).properties.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadOwnerProperties emette Error quando il usecase fallisce`() = runTest {
        coEvery { getOwnerPropertiesUseCase("owner-1") } returns Result.failure(
            Exception("Firestore offline")
        )

        viewModel.loadOwnerProperties("owner-1")
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is PropertyUiState.Error)
            assertEquals("Firestore offline", (state as PropertyUiState.Error).message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── createProperty ──────────────────────────────────────────────────

    @Test
    fun `createProperty ricarica le proprietà del proprietario dopo il successo`() = runTest {
        coEvery { createPropertyUseCase(sampleProperty) } returns Result.success("prop-1")
        coEvery { getOwnerPropertiesUseCase("owner-1") } returns Result.success(samplePropertyList)

        viewModel.createProperty(sampleProperty, "owner-1")
        advanceUntilIdle()

        // Dopo la creazione, deve aver richiamato loadOwnerProperties
        coVerify(exactly = 1) { createPropertyUseCase(sampleProperty) }
        coVerify(exactly = 1) { getOwnerPropertiesUseCase("owner-1") }

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is PropertyUiState.OwnerSuccess)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `createProperty emette Error quando la creazione fallisce`() = runTest {
        coEvery { createPropertyUseCase(sampleProperty) } returns Result.failure(
            Exception("Quota exceeded")
        )

        viewModel.createProperty(sampleProperty, "owner-1")
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is PropertyUiState.Error)
            assertEquals("Quota exceeded", (state as PropertyUiState.Error).message)
            cancelAndIgnoreRemainingEvents()
        }

        // Non deve aver richiamato loadOwnerProperties
        coVerify(exactly = 0) { getOwnerPropertiesUseCase(any()) }
    }

    // ── wishlist / saved properties ──────────────────────────────────────

    @Test
    fun `checkIfSaved updates isSaved state flow successfully`() = runTest {
        val dummyUser = User(id = "user-1", name = "Mario", email = "m@m.com", roles = emptyList())
        coEvery { userRepo.getCurrentUser() } returns dummyUser
        coEvery { wishlistRepo.isPropertySaved("user-1", "prop-1") } returns Result.success(true)

        viewModel.checkIfSaved("prop-1")
        advanceUntilIdle()

        viewModel.isSaved.test {
            assertEquals(true, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `toggleSaved updates isSaved state flow successfully`() = runTest {
        val dummyUser = User(id = "user-1", name = "Mario", email = "m@m.com", roles = emptyList())
        coEvery { userRepo.getCurrentUser() } returns dummyUser
        coEvery { wishlistRepo.toggleSavedProperty("user-1", "prop-1") } returns Result.success(true)

        viewModel.toggleSaved("prop-1")
        advanceUntilIdle()

        viewModel.isSaved.test {
            assertEquals(true, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
