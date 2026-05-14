package com.mobile.micasaestucasa.domain.usecase.wishlist

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class GetWishlistDataUseCaseTest {

    private val useCase = GetWishlistDataUseCase()

    @Test
    fun `invoke should return empty lists successfully`() = runTest {
        val result = useCase()

        assertTrue(result.isSuccess)
        val (properties, collections) = result.getOrThrow()

        assertTrue(properties.isEmpty())
        assertTrue(collections.isEmpty())
    }
}
