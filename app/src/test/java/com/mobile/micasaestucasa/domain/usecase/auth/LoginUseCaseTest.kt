package com.mobile.micasaestucasa.domain.usecase.auth

import com.mobile.micasaestucasa.domain.repository.auth.AuthRepo
import com.mobile.micasaestucasa.domain.repository.user.UserRepo
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LoginUseCaseTest {
    private lateinit var authRepo: AuthRepo
    private lateinit var userRepo: UserRepo
    private lateinit var loginUseCase: LoginUseCase

    @Before
    fun setUp() {
        authRepo = mockk()
        userRepo = mockk()
        loginUseCase = LoginUseCase(authRepo, userRepo)
    }

    @Test
    fun `login con credenziali valide ritorna successo`() = runTest {
        coEvery { authRepo.login(any(), any()) } returns Result.success(Unit)
        coEvery { userRepo.getCurrentUser() } returns null
        val result = loginUseCase("test@email.com", "Password123!")
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { authRepo.login("test@email.com", "Password123!") }
    }

    @Test
    fun `login email vuota ritorna failure senza chiamare repo`() = runTest {
        val result = loginUseCase("", "Password123!")
        assertTrue(result.isFailure)
        coVerify(exactly = 0) { authRepo.login(any(), any()) }
    }

    @Test
    fun `login pw vuota ritorna failure senza chaimare repo`() = runTest {
        val result = loginUseCase("test@email.com", "")
        assertTrue(result.isFailure)
        coVerify(exactly = 0) { authRepo.login(any(), any()) }
    }

    @Test
    fun `propogare errore firebase`() = runTest {
        val firebaseError = Exception("Firebase error")
        coEvery { authRepo.login(any(), any()) } returns Result.failure(firebaseError)
        val result = loginUseCase("test@email.com", "Password123!")
        assertTrue(result.isFailure)
    }
}
