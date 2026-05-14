package com.mobile.micasaestucasa.ui.viewmodels.wishlist

import app.cash.turbine.test
import com.mobile.micasaestucasa.domain.usecase.wishlist.GetWishlistDataUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before

@OptIn(ExperimentalCoroutinesApi::class)
class WishlistViewModelTest {

    private val GetWishlistDataUseCase = mockk<GetWishlistDataUseCase>()
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    fun `loadWishlist updates uiState to success when use case returns data`() = runTest {
        coEvery { GetWishlistDataUseCase() } returns Result.success(Pair(emptyList(), emptyList()))

        val viewModel = WishlistViewModel(GetWishlistDataUseCase)

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(false, state.isLoading)
            assertEquals(0, state.properties.size)
            assertEquals(null, state.error)
        }
    }
}
