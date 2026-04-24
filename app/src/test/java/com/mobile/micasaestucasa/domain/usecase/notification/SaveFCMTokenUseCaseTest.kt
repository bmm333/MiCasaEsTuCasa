package com.mobile.micasaestucasa.domain.usecase.notification

import com.mobile.micasaestucasa.domain.repository.notification.NotificationRepo
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SaveFCMTokenUseCaseTest {

    private lateinit var notificationRepo: NotificationRepo
    private lateinit var saveFCMTokenUseCase: SaveFCMTokenUseCase

    @Before
    fun setUp() {
        notificationRepo = mockk()
        saveFCMTokenUseCase = SaveFCMTokenUseCase(notificationRepo)
    }

    @Test
    fun `salvataggio token con parametri validi ritorna successo`() = runTest {
        coEvery { notificationRepo.saveFCMToken(any(), any()) } returns Result.success(Unit)

        val result = saveFCMTokenUseCase("user123", "fcm-token-abc")

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { notificationRepo.saveFCMToken("user123", "fcm-token-abc") }
    }

    @Test
    fun `userId vuoto ritorna failure senza chiamare repo`() = runTest {
        val result = saveFCMTokenUseCase("", "fcm-token-abc")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        assertEquals("userID not valid", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { notificationRepo.saveFCMToken(any(), any()) }
    }

    @Test
    fun `userId blank ritorna failure senza chiamare repo`() = runTest {
        val result = saveFCMTokenUseCase("   ", "fcm-token-abc")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        coVerify(exactly = 0) { notificationRepo.saveFCMToken(any(), any()) }
    }

    @Test
    fun `token vuoto ritorna failure senza chiamare repo`() = runTest {
        val result = saveFCMTokenUseCase("user123", "")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        assertEquals("Invalid FCM token", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { notificationRepo.saveFCMToken(any(), any()) }
    }

    @Test
    fun `token blank ritorna failure senza chiamare repo`() = runTest {
        val result = saveFCMTokenUseCase("user123", "   ")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        coVerify(exactly = 0) { notificationRepo.saveFCMToken(any(), any()) }
    }

    @Test
    fun `entrambi parametri vuoti ritorna failure per userId`() = runTest {
        val result = saveFCMTokenUseCase("", "")

        assertTrue(result.isFailure)
        assertEquals("userID not valid", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { notificationRepo.saveFCMToken(any(), any()) }
    }

    @Test
    fun `propagare errore firestore dal repo`() = runTest {
        val firestoreError = Exception("Firestore write failed")
        coEvery { notificationRepo.saveFCMToken(any(), any()) } returns Result.failure(firestoreError)

        val result = saveFCMTokenUseCase("user123", "fcm-token-abc")

        assertTrue(result.isFailure)
        assertEquals("Firestore write failed", result.exceptionOrNull()?.message)
        coVerify(exactly = 1) { notificationRepo.saveFCMToken("user123", "fcm-token-abc") }
    }
}
