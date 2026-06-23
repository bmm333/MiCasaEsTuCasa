package com.mobile.micasaestucasa.ui.viewmodels

import com.google.firebase.auth.FirebaseAuth
import com.mobile.micasaestucasa.domain.model.user.User
import com.mobile.micasaestucasa.domain.model.user.UserRole
import com.mobile.micasaestucasa.domain.repository.notification.NotificationRepo
import com.mobile.micasaestucasa.domain.repository.user.UserRepo
import com.mobile.micasaestucasa.domain.usecase.notification.SaveFCMTokenUseCase
import com.mobile.micasaestucasa.ui.navigation.Route
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        firebaseAuth: FirebaseAuth,
        userRepo: UserRepo
    ): MainViewModel {
        val saveFCMTokenUseCase = mockk<SaveFCMTokenUseCase>(relaxed = true)
        val notificationRepo = mockk<NotificationRepo>(relaxed = true)
        coEvery { notificationRepo.getCurrentToken() } returns Result.success("token")
        return MainViewModel(firebaseAuth, userRepo, saveFCMTokenUseCase, notificationRepo)
    }

    @Test
    fun `se non c'e una sessione iniziale la destinazione parte dal login`() = runTest {
        val firebaseAuth = mockk<FirebaseAuth>()
        val userRepo = mockk<UserRepo>()
        every { firebaseAuth.currentUser } returns null

        val viewModel = createViewModel(firebaseAuth, userRepo)
        advanceUntilIdle()

        assertEquals(Route.Login, viewModel.startDestination.value)
    }

    @Test
    fun `se esiste una sessione con profilo completo la destinazione parte dalla home`() = runTest {
        val firebaseAuth = mockk<FirebaseAuth>()
        val userRepo = mockk<UserRepo>()
        every { firebaseAuth.currentUser } returns mockk(relaxed = true)
        coEvery { userRepo.getCurrentUser() } returns User(
            id = "1",
            name = "Mario",
            email = "m@example.com",
            roles = listOf(UserRole.GUEST),
            profileCompleted = true
        )

        val viewModel = createViewModel(firebaseAuth, userRepo)
        advanceUntilIdle()

        assertEquals(Route.Home, viewModel.startDestination.value)
    }

    @Test
    fun `se esiste una sessione con profilo incompleto la destinazione parte dall onboarding`() = runTest {
        val firebaseAuth = mockk<FirebaseAuth>()
        val userRepo = mockk<UserRepo>()
        every { firebaseAuth.currentUser } returns mockk(relaxed = true)
        coEvery { userRepo.getCurrentUser() } returns User(
            id = "1",
            name = "Mario",
            email = "m@example.com",
            roles = listOf(UserRole.GUEST),
            profileCompleted = false
        )

        val viewModel = createViewModel(firebaseAuth, userRepo)
        advanceUntilIdle()

        assertEquals(Route.SignupOnboarding, viewModel.startDestination.value)
    }
}
