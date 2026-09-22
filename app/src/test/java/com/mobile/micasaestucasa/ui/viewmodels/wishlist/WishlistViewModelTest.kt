package com.mobile.micasaestucasa.ui.viewmodels.wishlist

import app.cash.turbine.test
import com.mobile.micasaestucasa.domain.repository.user.UserRepo
import com.mobile.micasaestucasa.domain.repository.whishlist.WhishlistRepo
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
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WishlistViewModelTest {

    private val getWishlistDataUseCase = mockk<GetWishlistDataUseCase>()
    private val wishlistRepo = mockk<WhishlistRepo>(relaxed = true)
    private val userRepo = mockk<UserRepo>(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadWishlist updates uiState to success when use case returns data`() = runTest {
        coEvery { getWishlistDataUseCase() } returns Result.success(Pair(emptyList(), emptyList()))

        val viewModel = WishlistViewModel(getWishlistDataUseCase, wishlistRepo, userRepo)

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(false, state.isLoading)
            assertEquals(0, state.properties.size)
            assertEquals(null, state.error)
        }
    }
}
