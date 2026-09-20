package com.mobile.micasaestucasa.ui.viewmodels.auth

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.mobile.micasaestucasa.domain.repository.notification.NotificationRepo
import com.mobile.micasaestucasa.domain.usecase.auth.DeleteAccountUseCase
import com.mobile.micasaestucasa.domain.usecase.auth.LoginUseCase
import com.mobile.micasaestucasa.domain.usecase.auth.LogoutUseCase
import com.mobile.micasaestucasa.domain.usecase.auth.RegisterUseCase
import com.mobile.micasaestucasa.domain.usecase.auth.ResetPasswordUseCase
import com.mobile.micasaestucasa.domain.usecase.auth.SignInWithGoogleUseCase
import com.mobile.micasaestucasa.domain.usecase.notification.SaveFCMTokenUseCase
import com.mobile.micasaestucasa.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private lateinit var loginUseCase: LoginUseCase
    private lateinit var registerUseCase: RegisterUseCase
    private lateinit var logoutUseCase: LogoutUseCase
    private lateinit var saveFCMTokenUseCase: SaveFCMTokenUseCase
    private lateinit var notificationRepo: NotificationRepo
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var viewModel: AuthViewModel
    private lateinit var resetPasswordUseCase: ResetPasswordUseCase
    private lateinit var signInWithGoogleUseCase: SignInWithGoogleUseCase
    private lateinit var deleteAccountUseCase: DeleteAccountUseCase

    @Before
    fun setUp() {
        loginUseCase = mockk()
        registerUseCase = mockk()
        logoutUseCase = mockk(relaxed = true)
        saveFCMTokenUseCase = mockk(relaxed = true)
        notificationRepo = mockk(relaxed = true)
        firebaseAuth = mockk(relaxed = true)
        resetPasswordUseCase = mockk(relaxed = true)
        signInWithGoogleUseCase = mockk(relaxed = true)
        deleteAccountUseCase = mockk(relaxed = true)
        every { firebaseAuth.currentUser?.uid } returns "user-123"
        coEvery { notificationRepo.getCurrentToken() } returns Result.success("fcm-token")
        val resetPasswordUseCase = mockk<com.mobile.micasaestucasa.domain.usecase.auth.ResetPasswordUseCase>(relaxed = true)
        viewModel = AuthViewModel(
            loginUseCase,
            registerUseCase,
            logoutUseCase,
            resetPasswordUseCase,
            signInWithGoogleUseCase,
            saveFCMTokenUseCase,
            notificationRepo,
            firebaseAuth,
            deleteAccountUseCase
        )

        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
    }

    @After
    fun tearDown() {
        unmockkStatic(Log::class)
    }

    @Test
    fun `login con credenziali valide aggiorna lo stato con successo`() = runTest {
        coEvery { loginUseCase("test@email.com", "Password123!") } returns Result.success(Unit)

        viewModel.login("test@email.com", "Password123!")
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.isAuthSuccessful.value)
        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.errorMessage.value)
        coVerify(exactly = 1) { loginUseCase("test@email.com", "Password123!") }
    }

    @Test
    fun `login con credenziali errate mostra il messaggio di errore`() = runTest {
        coEvery { loginUseCase("test@email.com", "Password123!") } returns Result.failure(Exception("Credenziali non valide"))

        viewModel.login("test@email.com", "Password123!")
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.isAuthSuccessful.value)
        assertFalse(viewModel.isLoading.value)
        assertEquals("Credenziali non valide", viewModel.errorMessage.value)
        coVerify(exactly = 1) { loginUseCase("test@email.com", "Password123!") }
    }

    @Test
    fun `register con dati validi aggiorna lo stato con successo`() = runTest {
        coEvery { registerUseCase("nuovo@email.com", "Password123!") } returns Result.success(Unit)

        viewModel.register("nuovo@email.com", "Password123!")
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.isAuthSuccessful.value)
        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.errorMessage.value)
        coVerify(exactly = 1) { registerUseCase("nuovo@email.com", "Password123!") }
    }

    @Test
    fun `register con dati non validi mostra l'errore corretto`() = runTest {
        coEvery { registerUseCase("nuovo@email.com", "Password123!") } returns Result.failure(Exception("Email già in uso"))

        viewModel.register("nuovo@email.com", "Password123!")
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.isAuthSuccessful.value)
        assertFalse(viewModel.isLoading.value)
        assertEquals("Email già in uso", viewModel.errorMessage.value)
        coVerify(exactly = 1) { registerUseCase("nuovo@email.com", "Password123!") }
    }

    @Test
    fun `logout chiama il caso d'uso e azzera l'autenticazione`() = runTest {
        coEvery { loginUseCase("test@email.com", "Password123!") } returns Result.success(Unit)

        viewModel.login("test@email.com", "Password123!")
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.isAuthSuccessful.value)

        viewModel.logout()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.isAuthSuccessful.value)
        coVerify(exactly = 1) { logoutUseCase() }
    }

    @Test
    fun `clearError pulisce il messaggio di errore`() = runTest {
        coEvery { loginUseCase(any(), any()) } returns Result.failure(Exception("Errore generico"))

        viewModel.login("test@email.com", "Password123!")
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()
        viewModel.clearError()

        assertNull(viewModel.errorMessage.value)
    }

    @Test
    fun `login durante l'esecuzione imposta loading a true`() = runTest {
        val pendingResult = CompletableDeferred<Result<Unit>>()
        coEvery { loginUseCase("test@email.com", "Password123!") } coAnswers { pendingResult.await() }

        val job = launch { viewModel.login("test@email.com", "Password123!") }
        runCurrent()

        assertTrue(viewModel.isLoading.value)
        pendingResult.complete(Result.success(Unit))
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()
        job.join()

        assertFalse(viewModel.isLoading.value)
    }
}
