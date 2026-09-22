package com.mobile.micasaestucasa.domain.usecase.auth

import com.google.firebase.auth.FirebaseAuth
import com.mobile.micasaestucasa.domain.repository.auth.AuthRepo
import com.mobile.micasaestucasa.domain.usecase.notification.RemoveFCMTokenUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class LogoutUseCaseTest {

    private lateinit var authRepo: AuthRepo
    private lateinit var removeFCMTokenUseCase: RemoveFCMTokenUseCase
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var logoutUseCase: LogoutUseCase

    @Before
    fun setUp() {
        authRepo = mockk()
        removeFCMTokenUseCase = mockk(relaxed = true)
        firebaseAuth = mockk(relaxed = true)
        every { firebaseAuth.currentUser?.uid } returns "user-123"
        logoutUseCase = LogoutUseCase(authRepo, removeFCMTokenUseCase, firebaseAuth)
    }

    @Test
    fun `logout rimuove il token FCM e delega al repository`() = runTest {
        coEvery { authRepo.logout() } returns Unit
        coEvery { removeFCMTokenUseCase("user-123") } returns Result.success(Unit)

        logoutUseCase()

        coVerify(exactly = 1) { removeFCMTokenUseCase("user-123") }
        coVerify(exactly = 1) { authRepo.logout() }
    }
}
