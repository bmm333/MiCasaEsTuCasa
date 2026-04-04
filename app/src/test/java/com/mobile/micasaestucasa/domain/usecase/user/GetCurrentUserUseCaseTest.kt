package com.mobile.micasaestucasa.domain.usecase.user

import com.mobile.micasaestucasa.domain.model.user.User
import com.mobile.micasaestucasa.domain.model.user.UserRole
import com.mobile.micasaestucasa.domain.repository.user.UserRepo
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test

class GetCurrentUserUseCaseTest {
    //mock l'interfaccia DIP
    private val userRepository = mockk<UserRepo>()
    private val useCase = GetCurrentUserUseCase(userRepository)


    @Test
    fun `Repo returns user, usecase should return same user`() = runTest {
        // GIVEN (Arranage)
        val expectedUser = User(
            id = "1",
            name = "John Doe",
            email = "john@example.com",
            roles = listOf(UserRole.ADMIN)
        )
        coEvery { userRepository.getCurrentUser() } returns expectedUser
        val result = useCase()
        Assert.assertEquals(expectedUser, result)
    }
    @Test
    fun `when repo returns null, usecase should return null`() = runTest {
        coEvery { userRepository.getCurrentUser() } returns null
        val result = useCase()

        Assert.assertEquals(null, result)
    }
}