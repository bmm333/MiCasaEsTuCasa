package com.mobile.micasaestucasa.data.repository.auth

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FirebaseAuthRepoTest {

    private val firebaseAuth = mockk<FirebaseAuth>()
    private val firestore = mockk<FirebaseFirestore>()
    private val repo = FirebaseAuthRepo(firebaseAuth, firestore)

    @Test
    fun `login riuscito ritorna success`() = runTest {
        every {
            firebaseAuth.signInWithEmailAndPassword("test@email.com", "Password123!")
        } returns Tasks.forResult(mockk<AuthResult>(relaxed = true))

        val result = repo.login("test@email.com", "Password123!")

        assertTrue(result.isSuccess)
    }

    @Test
    fun `login con errore firebase ritorna failure`() = runTest {
        every {
            firebaseAuth.signInWithEmailAndPassword("test@email.com", "Password123!")
        } returns Tasks.forException(RuntimeException("Credenziali errate"))

        val result = repo.login("test@email.com", "Password123!")

        assertTrue(result.isFailure)
        assertEquals("Credenziali errate", result.exceptionOrNull()?.message)
    }

    @Test
    fun `register riuscita ritorna success con uid`() = runTest {
        val mockUser = mockk<FirebaseUser> {
            every { uid } returns "test-uid"
        }
        val mockAuthResult = mockk<AuthResult> {
            every { user } returns mockUser
        }

        every {
            firebaseAuth.createUserWithEmailAndPassword("test@email.com", "Password123!")
        } returns Tasks.forResult(mockAuthResult)

        val result = repo.register("test@email.com", "Password123!")

        assertTrue(result.isSuccess)
        assertEquals("test-uid", result.getOrNull())
    }

    @Test
    fun `register con errore firebase ritorna failure`() = runTest {
        every {
            firebaseAuth.createUserWithEmailAndPassword("test@email.com", "Password123!")
        } returns Tasks.forException(RuntimeException("Email in uso"))

        val result = repo.register("test@email.com", "Password123!")

        assertTrue(result.isFailure)
        assertEquals("Email in uso", result.exceptionOrNull()?.message)
    }

    @Test
    fun `logout invoca signOut su firebaseAuth`() = runTest {
        every { firebaseAuth.signOut() } just runs

        repo.logout()

        verify(exactly = 1) { firebaseAuth.signOut() }
    }
}
