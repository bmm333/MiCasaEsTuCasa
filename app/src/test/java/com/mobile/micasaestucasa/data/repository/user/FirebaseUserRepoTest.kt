package com.mobile.micasaestucasa.data.repository.user

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.mobile.micasaestucasa.data.dto.user.UserDTO
import com.mobile.micasaestucasa.domain.model.user.UserRole
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FirebaseUserRepoTest {

    private val firebaseAuth = mockk<FirebaseAuth>()
    private val firestore = mockk<FirebaseFirestore>()

    private val repo = FirebaseUserRepo(
        firebaseAuth,
        firestore
    )

    @Test
    fun `getCurrentUser con utente nullo ritorna null`() = runTest {
        every { firebaseAuth.currentUser } returns null

        val result = repo.getCurrentUser()

        assertNull(result)
    }

    @Test
    fun `getCurrentUser mappa utente firestore in domain`() = runTest {
        val firebaseUser = mockk<FirebaseUser>()
        val collectionRef = mockk<CollectionReference>()
        val documentRef = mockk<DocumentReference>()
        val snapshot = mockk<DocumentSnapshot>()
        val userDto = UserDTO(
            id = "uid-1",
            name = "Mario",
            email = "mario@test.com",
            roles = listOf("GUEST")
        )
        every { firebaseAuth.currentUser } returns firebaseUser
        every { firebaseUser.uid } returns "uid-1"
        every { firestore.collection("users") } returns collectionRef
        every { collectionRef.document("uid-1") } returns documentRef
        every { documentRef.get() } returns Tasks.forResult(snapshot)
        every {
            snapshot.toObject(UserDTO::class.java)
        } returns userDto
        val result = repo.getCurrentUser()
        println(result)
        requireNotNull(result)
        assertEquals("uid-1", result.id)
        assertEquals("Mario", result.name)
        assertEquals("mario@test.com", result.email)
    }
}
