package com.mobile.micasaestucasa.domain.usecase.auth

import com.mobile.micasaestucasa.domain.repository.auth.AuthRepo
import com.mobile.micasaestucasa.domain.repository.user.UserRepo
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
    private lateinit var userRepo: UserRepo
    private lateinit var registerUseCase: RegisterUseCase

    @Before
    fun setUp() {
        authRepo = mockk()
        userRepo = mockk()
        registerUseCase = RegisterUseCase(authRepo, userRepo)
    }

    @Test
    fun `register con dati validi crea utente su auth e firestore`() = runTest {
        val uid = "test-uid"
        coEvery { authRepo.register(any(), any()) } returns Result.success(uid)
        coEvery { userRepo.updateUserProfile(any()) } returns Result.success(Unit)

        val result = registerUseCase("test@email.com", "Password123!")

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { authRepo.register("test@email.com", "Password123!") }
        coVerify(exactly = 1) { userRepo.updateUserProfile(match { it.id == uid && it.email == "test@email.com" }) }
    }

    @Test
    fun `register con email non valida ritorna failure`() = runTest {
        val result = registerUseCase("test", "Password123!")
        assertTrue(result.isFailure)
        coVerify(exactly = 0) { authRepo.register(any(), any()) }
        coVerify(exactly = 0) { userRepo.updateUserProfile(any()) }
    }

    @Test
    fun `register con pw non valida ritorna failure`() = runTest {
        val result = registerUseCase("test@email.com", "Pasd")
        assertTrue(result.isFailure)
        coVerify(exactly = 0) { authRepo.register(any(), any()) }
    }

    @Test
    fun `register con campi vuoti ritorna failure`() = runTest {
        val result = registerUseCase("", "")
        assertTrue(result.isFailure)
        coVerify(exactly = 0) { authRepo.register(any(), any()) }
    }

    @Test
    fun `register con email valida formato complesso`() = runTest {
        coEvery { authRepo.register(any(), any()) } returns Result.success("uid")
        coEvery { userRepo.updateUserProfile(any()) } returns Result.success(Unit)

        val result = registerUseCase("user.name+tag@sub.domain.com", "password123")

        assertTrue(result.isSuccess)
    }

    @Test
    fun `register propaga errore del repo auth`() = runTest {
        coEvery { authRepo.register(any(), any()) } returns Result.failure(Exception("Email già in uso"))

        val result = registerUseCase("test@email.com", "password123")

        assertTrue(result.isFailure)
        assertEquals("Email già in uso", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { userRepo.updateUserProfile(any()) }
    }

    @Test
    fun `register propaga errore del repo user`() = runTest {
        coEvery { authRepo.register(any(), any()) } returns Result.success("uid")
        coEvery { userRepo.updateUserProfile(any()) } returns Result.failure(Exception("Firestore error"))

        val result = registerUseCase("test@email.com", "password123")

        assertTrue(result.isFailure)
        assertEquals("Firestore error", result.exceptionOrNull()?.message)
    }
}
