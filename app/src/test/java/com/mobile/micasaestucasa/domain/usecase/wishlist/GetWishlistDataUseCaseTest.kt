package com.mobile.micasaestucasa.domain.usecase.wishlist

import com.mobile.micasaestucasa.domain.model.user.User
import com.mobile.micasaestucasa.domain.repository.user.UserRepo
import com.mobile.micasaestucasa.domain.repository.whishlist.WhishlistRepo
import com.mobile.micasaestucasa.domain.model.user.UserRole
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class GetWishlistDataUseCaseTest {

    private val wishlistRepo: WhishlistRepo = mockk()
    private val userRepo: UserRepo = mockk()
    private val useCase = GetWishlistDataUseCase(wishlistRepo, userRepo)

    @Test
    fun `invoke should return empty lists successfully`() = runTest {
        val user = User(id = "user1", name = "Test", email = "test@example.com", roles = listOf(UserRole.GUEST))
        coEvery { userRepo.getCurrentUser() } returns user
        coEvery { wishlistRepo.getSavedProperties("user1") } returns Result.success(emptyList())
        coEvery { wishlistRepo.getCollections("user1") } returns Result.success(emptyList())

        val result = useCase()

        assertTrue(result.isSuccess)
        val (properties, collections) = result.getOrThrow()

        assertTrue(properties.isEmpty())
        assertTrue(collections.isEmpty())
    }
}
