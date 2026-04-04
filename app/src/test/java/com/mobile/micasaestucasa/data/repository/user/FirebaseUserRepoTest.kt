package com.mobile.micasaestucasa.data.repository.user

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.mobile.micasaestucasa.domain.model.user.UserRole
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FirebaseUserRepoTest {

    private val firebaseAuth = mockk<FirebaseAuth>()
    private val repo = FirebaseUserRepo(firebaseAuth)

    @Test
    fun `getCurrentUser con utente nullo ritorna null`() = runTest {
        every { firebaseAuth.currentUser } returns null

        val result = repo.getCurrentUser()

        assertNull(result)
    }

    @Test
    fun `getCurrentUser mappa firebase user in domain con ruolo guest`() = runTest {
        val firebaseUser = mockk<FirebaseUser>()
        every { firebaseAuth.currentUser } returns firebaseUser
        every { firebaseUser.uid } returns "uid-1"
        every { firebaseUser.displayName } returns "Mario"
        every { firebaseUser.email } returns "mario@test.com"

        val result = repo.getCurrentUser()

        requireNotNull(result)
        assertEquals("uid-1", result.id)
        assertEquals("Mario", result.name)
        assertEquals("mario@test.com", result.email)
        assertEquals(listOf(UserRole.GUEST), result.roles)
    }
}
