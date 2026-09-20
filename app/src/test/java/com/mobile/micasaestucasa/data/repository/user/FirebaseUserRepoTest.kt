package com.mobile.micasaestucasa.data.repository.user

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.mobile.micasaestucasa.data.dto.user.UserDTO
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class FirebaseUserRepoTest {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var collection: CollectionReference
    private lateinit var repo: FirebaseUserRepo

    @Before
    fun setUp() {
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")
        firebaseAuth = mockk(relaxed = true)
        firestore = mockk(relaxed = true)
        collection = mockk(relaxed = true)
        every { firestore.collection("users") } returns collection
        repo = FirebaseUserRepo(firebaseAuth, firestore)
    }

    @After
    fun tearDown() {
        unmockkStatic("kotlinx.coroutines.tasks.TasksKt")
    }

    @Test
    fun `getCurrentUser con utente nullo ritorna null`() = runTest {
        every { firebaseAuth.currentUser } returns null

        val result = repo.getCurrentUser()

        assertNull(result)
    }

    @Test
    fun `getCurrentUser mappa firebase user in domain`() = runTest {
        val firebaseUser = mockk<FirebaseUser>()
        val docRef = mockk<DocumentReference>()
        val docSnapshot = mockk<DocumentSnapshot>()
        val userDto = UserDTO(id = "uid-1", name = "Mario", email = "mario@test.com")

        every { firebaseAuth.currentUser } returns firebaseUser
        every { firebaseUser.uid } returns "uid-1"
        every { collection.document("uid-1") } returns docRef
        every { docRef.get() } returns Tasks.forResult(docSnapshot)
        every { docSnapshot.toObject(UserDTO::class.java) } returns userDto

        val result = repo.getCurrentUser()

        if (result != null) {
            assertEquals("uid-1", result.id)
            assertEquals("mario@test.com", result.email)
            assertEquals("Mario", result.name)
        }
    }
}
