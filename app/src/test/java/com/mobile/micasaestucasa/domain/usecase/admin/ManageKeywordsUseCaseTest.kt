package com.mobile.micasaestucasa.domain.usecase.admin

import com.mobile.micasaestucasa.domain.model.admin.Keyword
import com.mobile.micasaestucasa.domain.model.user.User
import com.mobile.micasaestucasa.domain.model.user.UserRole
import com.mobile.micasaestucasa.domain.repository.admin.AdminRepo
import com.mobile.micasaestucasa.domain.repository.user.UserRepo
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ManageKeywordsUseCaseTest {

    private lateinit var adminRepo: AdminRepo
    private lateinit var userRepo: UserRepo
    private lateinit var useCase: ManageKeywordsUseCase

    private val adminUser = User(
        id = "admin1",
        name = "Admin",
        email = "admin@micasa.it",
        roles = listOf(UserRole.ADMIN)
    )
    private val regularUser = User(
        id = "user1",
        name = "User",
        email = "user@micasa.it",
        roles = listOf(UserRole.GUEST)
    )

    @Before
    fun setUp() {
        adminRepo = mockk()
        userRepo = mockk()
        useCase = ManageKeywordsUseCase(adminRepo, userRepo)
    }

    @Test
    fun `addKeyword from admin with valid label returns id`() = runTest {
        coEvery { userRepo.getCurrentUser() } returns adminUser
        coEvery { adminRepo.addKeyword(any(), any()) } returns Result.success("kw1")
        val result = useCase.addKeyword("wifi", "admin1")
        assertTrue(result.isSuccess)
        assertEquals("kw1", result.getOrNull())
    }

    @Test
    fun `addkw passes label as is to repo`() = runTest {
        coEvery { userRepo.getCurrentUser() } returns adminUser
        coEvery { adminRepo.addKeyword("PISCINA", "admin1") } returns Result.success("kw2")
        val result = useCase.addKeyword("PISCINA", "admin1")
        assertTrue(result.isSuccess)
        coVerify { adminRepo.addKeyword("PISCINA", "admin1") }
    }

    @Test
    fun `blank keyword label returns failure`() = runTest {
        val result = useCase.addKeyword("", "admin1")
        assertTrue(result.isFailure)
        assertEquals("KEyword lalel  cannot be blank", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { adminRepo.addKeyword(any(), any()) }
    }

    @Test
    fun `addkeyword from non admin returns failure`() = runTest {
        coEvery { userRepo.getCurrentUser() } returns regularUser
        val result = useCase.addKeyword("wifi", "user1")
        assertTrue(result.isFailure)
        assertEquals("Access denied", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) {
            adminRepo.addKeyword(any(), any())
        }
    }

    @Test
    fun `delete keyword with kwid blank returns failure`() = runTest {
        val result = useCase.deleteKeyword("", "admin1")
        assertTrue(result.isFailure)
        assertEquals("Keywordid not valid", result.exceptionOrNull()?.message)
    }

    @Test
    fun `delete kw from non admin returns SE`() = runTest {
        coEvery { userRepo.getCurrentUser() } returns regularUser
        val result = useCase.deleteKeyword("kw1", "user1")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is SecurityException)
        coVerify(exactly = 0) { adminRepo.deleteKeyword(any(), any()) }
    }

    @Test
    fun `deleteKeyword from admin returns success`() = runTest {
        coEvery { userRepo.getCurrentUser() } returns adminUser
        coEvery { adminRepo.deleteKeyword("kw1", "admin1") } returns Result.success(Unit)
        val result = useCase.deleteKeyword("kw1", "admin1")
        assertTrue(result.isSuccess)
    }

    @Test
    fun `getAllKeywords accessible to everyone doesnt need admin`() = runTest {
        val keywords = listOf(
            Keyword("k1", "wifi"),
            Keyword("k2", "piscina")
        )
        coEvery { adminRepo.getAllKeywords() } returns Result.success(keywords)
        val result = useCase.getAllKeywords()
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
        coVerify(exactly = 0) { userRepo.getCurrentUser() }
    }

    @Test
    fun `getAllKeywords propogates Firestore Error`() = runTest {
        coEvery { adminRepo.getAllKeywords() } returns
            Result.failure(Exception("Firestore unavailable"))
        val result = useCase.getAllKeywords()
        assertTrue(result.isFailure)
    }
}
