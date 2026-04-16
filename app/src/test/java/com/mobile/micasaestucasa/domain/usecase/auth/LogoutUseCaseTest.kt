package com.mobile.micasaestucasa.domain.usecase.auth

import com.mobile.micasaestucasa.domain.repository.auth.AuthRepo
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class LogoutUseCaseTest {

    private lateinit var authRepo: AuthRepo
    private lateinit var logoutUseCase: LogoutUseCase

    @Before
    fun setUp() {
        authRepo = mockk()
        logoutUseCase = LogoutUseCase(authRepo)
    }

    @Test
    fun `logout delega al repository`() = runTest {
        coEvery { authRepo.logout() } returns Unit

        logoutUseCase()

        coVerify(exactly = 1) { authRepo.logout() }
    }
}
