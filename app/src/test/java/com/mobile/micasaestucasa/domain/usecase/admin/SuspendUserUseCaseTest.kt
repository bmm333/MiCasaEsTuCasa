package com.mobile.micasaestucasa.domain.usecase.admin

import com.mobile.micasaestucasa.domain.model.user.User
import com.mobile.micasaestucasa.domain.model.user.UserRole
import com.mobile.micasaestucasa.domain.repository.admin.AdminRepo
import com.mobile.micasaestucasa.domain.repository.user.UserRepo
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SuspendUserUseCaseTest {

    private lateinit var adminRepo: AdminRepo
    private lateinit var userRepo: UserRepo
    private lateinit var useCase: SuspendUserUseCase

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

    private val anotherAdmin = User(
        id = "admin2",
        name = "Admin 2",
        email = "admin2@micasa.it",
        roles = listOf(UserRole.ADMIN)
    )

    @Before
    fun setUp() {
        adminRepo = mockk()
        userRepo = mockk()
        useCase = SuspendUserUseCase(adminRepo, userRepo)
    }

    @Test
    fun `admin suspends a regular user successfully`() = runTest {
        coEvery { userRepo.getCurrentUser() } returns adminUser
        coEvery { userRepo.getUserById("user1") } returns Result.success(regularUser)
        coEvery { adminRepo.suspendUser("user1", "admin1") } returns Result.success(Unit)

        val result = useCase("user1", "admin1")

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { adminRepo.suspendUser("user1", "admin1") }
    }

    @Test
    fun `admin cant suspend themselves`() = runTest {
        coEvery { userRepo.getCurrentUser() } returns adminUser

        val result = useCase("admin1", "admin1")

        assertTrue(result.isFailure)
        assertEquals("Cannot suspend self", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { adminRepo.suspendUser(any(), any()) }
    }

    @Test
    fun `admin cant suspend another admin`() = runTest {
        coEvery { userRepo.getCurrentUser() } returns adminUser
        coEvery { userRepo.getUserById("admin2") } returns Result.success(anotherAdmin)

        val result = useCase("admin2", "admin1")

        assertTrue(result.isFailure)
        assertEquals(
            "Non puoi sospendere un altro admin",
            result.exceptionOrNull()?.message
        )
        coVerify(exactly = 0) { adminRepo.suspendUser(any(), any()) }
    }

    @Test
    fun `non admin user cant suspend anyone`() = runTest {
        coEvery { userRepo.getCurrentUser() } returns regularUser

        val result = useCase("user2", "user1")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is SecurityException)
        coVerify(exactly = 0) { adminRepo.suspendUser(any(), any()) }
    }

    @Test
    fun `not logged in user cant suspend`() = runTest {
        coEvery { userRepo.getCurrentUser() } returns null

        val result = useCase("user1", "admin1")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is SecurityException)
    }

    @Test
    fun `blank targetUserId returns failure`() = runTest {
        val result = useCase("", "admin1")

        assertTrue(result.isFailure)
        assertEquals("Id non validi", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { adminRepo.suspendUser(any(), any()) }
    }

    @Test
    fun `blank adminId returns failure`() = runTest {
        val result = useCase("user1", "")

        assertTrue(result.isFailure)
        assertEquals("Id non validi", result.exceptionOrNull()?.message)
    }

    @Test
    fun `firestore error in suspendUser gets propagated`() = runTest {
        coEvery { userRepo.getCurrentUser() } returns adminUser
        coEvery { userRepo.getUserById("user1") } returns Result.success(regularUser)
        coEvery { adminRepo.suspendUser(any(), any()) } returns
            Result.failure(Exception("Firestore unavailable"))

        val result = useCase("user1", "admin1")

        assertTrue(result.isFailure)
        assertEquals("Firestore unavailable", result.exceptionOrNull()?.message)
    }
}
