package com.mobile.micasaestucasa.ui.viewmodels

import com.google.firebase.auth.FirebaseAuth
import com.mobile.micasaestucasa.ui.navigation.Route
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test

class MainViewModelTest {

    @Test
    fun `se non c'e una sessione iniziale la destinazione parte dal login`() {
        val firebaseAuth = mockk<FirebaseAuth>()
        every { firebaseAuth.currentUser } returns null

        val viewModel = MainViewModel(firebaseAuth)

        assertEquals(Route.Login, viewModel.startDestination)
    }

    @Test
    fun `se esiste una sessione iniziale la destinazione parte dalla home`() {
        val firebaseAuth = mockk<FirebaseAuth>()
        every { firebaseAuth.currentUser } returns mockk(relaxed = true)

        val viewModel = MainViewModel(firebaseAuth)

        assertEquals(Route.Home, viewModel.startDestination)
    }
}