package com.mobile.micasaestucasa.domain.usecase.auth

import com.mobile.micasaestucasa.domain.repository.auth.AuthRepo
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RegisterUseCaseTest {
    private lateinit var authRepo: AuthRepo
    private lateinit var registerUseCase: RegisterUseCase

    @Before
    fun setUp(){
        authRepo= mockk()
        registerUseCase= RegisterUseCase(authRepo)
    }

    @Test
    fun `regiseter con dati validi ritorna success`()= runTest {
        coEvery { authRepo.register(any(),any()) } returns Result.success(Unit)
        val result=registerUseCase("test@email.com","Password123!")
        assertTrue(result.isSuccess)
        coVerify(exactly=1){authRepo.register("test@email.com","Password123!")}
    }

    @Test
    fun `register con email non valida ritonra failure`()=runTest {
        val result=registerUseCase("test","Password123!")
        assertTrue(result.isFailure)
        coVerify(exactly=0){authRepo.register(any(),any())}
    }

    @Test
    fun `register con pw non valida ritorna failure`()=runTest {
        val result=registerUseCase("test@email.com","Pasd")
        assertTrue(result.isFailure)
        coVerify(exactly=0){authRepo.register(any(),any())}
    }

    @Test
    fun `register con campi vuoti ritorna failure`()= runTest {
        val result=registerUseCase("","")
        assertTrue(result.isFailure)
        coVerify(exactly = 0) { authRepo.register(any(), any()) }
    }

    @Test
    fun `register con email valida formato complesso`() = runTest {
        coEvery { authRepo.register(any(), any()) } returns Result.success(Unit)

        val result = registerUseCase("user.name+tag@sub.domain.com", "password123")

        assertTrue(result.isSuccess)
    }

    @Test
    fun `register propaga errore del repo`() = runTest {
        coEvery { authRepo.register(any(), any()) } returns Result.failure(Exception("Email già in uso"))

        val result = registerUseCase("test@email.com", "password123")

        assertTrue(result.isFailure)
        assertEquals("Email già in uso", result.exceptionOrNull()?.message)
    }
}
