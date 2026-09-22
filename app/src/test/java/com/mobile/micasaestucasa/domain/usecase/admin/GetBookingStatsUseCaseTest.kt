package com.mobile.micasaestucasa.domain.usecase.admin

import com.mobile.micasaestucasa.domain.model.admin.BookingStats
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

class GetBookingStatsUseCaseTest {

    private lateinit var adminRepo: AdminRepo
    private lateinit var userRepo: UserRepo
    private lateinit var useCase: GetBookingStatsUseCase

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
        useCase = GetBookingStatsUseCase(adminRepo, userRepo)
    }

    @Test
    fun `admin gets booking stats correctly`() = runTest {
        val stats = BookingStats(
            total = 100,
            completed = 60,
            active = 20,
            pending = 10,
            cancelled = 7,
            rejected = 3
        )
        coEvery { userRepo.getCurrentUser() } returns adminUser
        coEvery { adminRepo.getBookingStats() } returns Result.success(stats)

        val result = useCase("admin1")

        assertTrue(result.isSuccess)
        val s = result.getOrThrow()
        assertEquals(100, s.total)
        assertEquals(60, s.completed)
        assertEquals(20, s.active)
        assertEquals(s.total, s.completed + s.active + s.pending + s.cancelled + s.rejected)
    }

    @Test
    fun `non admin cant see stats`() = runTest {
        coEvery { userRepo.getCurrentUser() } returns regularUser

        val result = useCase("user1")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is SecurityException)
        coVerify(exactly = 0) { adminRepo.getBookingStats() }
    }

    @Test
    fun `blank adminId returns failure`() = runTest {
        val result = useCase("")

        assertTrue(result.isFailure)
        assertEquals("AdminID not valid", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { adminRepo.getBookingStats() }
    }

    @Test
    fun `stats with zero bookings returns empty obj`() = runTest {
        coEvery { userRepo.getCurrentUser() } returns adminUser
        coEvery { adminRepo.getBookingStats() } returns
            Result.success(BookingStats())

        val result = useCase("admin1")

        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrThrow().total)
    }

    @Test
    fun `firestore error gets propagated correctly`() = runTest {
        coEvery { userRepo.getCurrentUser() } returns adminUser
        coEvery { adminRepo.getBookingStats() } returns
            Result.failure(Exception("Firestore unavailable"))

        val result = useCase("admin1")

        assertTrue(result.isFailure)
        assertEquals("Firestore unavailable", result.exceptionOrNull()?.message)
    }
}
