package com.mobile.micasaestucasa.ui.viewmodels.user

import com.mobile.micasaestucasa.domain.model.user.User
import com.mobile.micasaestucasa.domain.model.user.UserRole
import com.mobile.micasaestucasa.domain.usecase.user.GetCurrentUserUseCase
import com.mobile.micasaestucasa.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UserViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val getCurrentUserUseCase = mockk<GetCurrentUserUseCase>()

    @Test
    fun `caricamento utente con utente presente aggiorna lo stato`() = runTest {
        val expectedUser = User(
            id = "1",
            name = "Mario Rossi",
            email = "mario@example.com",
            roles = listOf(UserRole.GUEST)
        )
        coEvery { getCurrentUserUseCase() } returns expectedUser
        val viewModel = UserViewModel(getCurrentUserUseCase)

        viewModel.loadUser()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(expectedUser, viewModel.user.value)
    }

    @Test
    fun `caricamento utente senza sessione mantiene lo stato vuoto`() = runTest {
        coEvery { getCurrentUserUseCase() } returns null
        val viewModel = UserViewModel(getCurrentUserUseCase)

        viewModel.loadUser()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        assertNull(viewModel.user.value)
    }
}
