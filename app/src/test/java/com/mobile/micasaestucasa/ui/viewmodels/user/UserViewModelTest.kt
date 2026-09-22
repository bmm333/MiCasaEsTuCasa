package com.mobile.micasaestucasa.ui.viewmodels.user

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.mobile.micasaestucasa.domain.model.user.User
import com.mobile.micasaestucasa.domain.model.user.UserRole
import com.mobile.micasaestucasa.domain.repository.booking.BookingRepo
import com.mobile.micasaestucasa.domain.repository.property.PropertyRepo
import com.mobile.micasaestucasa.domain.repository.user.UserRepo
import com.mobile.micasaestucasa.domain.util.Resource
import com.mobile.micasaestucasa.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UserViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val userRepo = mockk<UserRepo>()
    private val propertyRepo = mockk<PropertyRepo>()
    private val bookingRepo = mockk<BookingRepo>()
    private val dataStore = mockk<DataStore<Preferences>>(relaxed = true)
    private lateinit var viewModel: UserViewModel

    private val testUser = User(
        id = "1",
        name = "Mario Rossi",
        email = "mario@example.com",
        roles = listOf(UserRole.GUEST),
        bio = "Bio di test",
        address = "Indirizzo test",
        phone = "123456"
    )

    @Before
    fun setup() {
        // Mock caricamento iniziale per il ViewModel init
        coEvery { userRepo.getCurrentUser() } returns testUser
        coEvery { propertyRepo.getPropertiesByOwner(any()) } returns Result.success(emptyList())
        io.mockk.every { dataStore.data } returns kotlinx.coroutines.flow.emptyFlow()
        io.mockk.coEvery { dataStore.updateData(any()) } returns androidx.datastore.preferences.core.emptyPreferences()
        viewModel = UserViewModel(userRepo, propertyRepo, bookingRepo, dataStore)
    }

    @Test
    fun `loadUser should update state to Success when repo returns user`() = runTest {
        // L'init chiama già loadUser, ma lo chiamiamo esplicitamente per chiarezza nel test
        viewModel.loadUser()
        advanceUntilIdle()

        val state = viewModel.userState.value
        assertTrue("Expected Resource.Success but was $state", state is Resource.Success)
        assertEquals(testUser, (state as Resource.Success).data)
    }

    @Test
    fun `loadUser should update state to Error when repo fails`() = runTest {
        val errorMsg = "Database Error"
        // Cambiamo il mock per far fallire la chiamata
        coEvery { userRepo.getCurrentUser() } throws Exception(errorMsg)

        viewModel.loadUser()
        advanceUntilIdle()

        val state = viewModel.userState.value
        assertTrue("Expected Resource.Error but was $state", state is Resource.Error)
        assertEquals(errorMsg, (state as Resource.Error).message)
    }

    @Test
    fun `updateProfile should update local state and call repository`() = runTest {
        val updatedUser = testUser.copy(name = "Updated Name")
        coEvery { userRepo.updateUserProfile(any()) } returns Result.success(Unit)

        viewModel.updateProfile(updatedUser)
        advanceUntilIdle()

        val state = viewModel.userState.value
        assertTrue("Expected Resource.Success but was $state", state is Resource.Success)
        val data = (state as Resource.Success).data
        assertEquals("Updated Name", data?.name)
        // Verifichiamo l'intero oggetto per evitare ComparisonFailure se altri campi differiscono
        assertEquals(updatedUser, data)
    }

    @Test
    fun `updateProfile should emit Error when repository fails`() = runTest {
        val updatedUser = testUser.copy(name = "Updated Name")
        coEvery { userRepo.updateUserProfile(any()) } returns Result.failure(Exception("Errore di rete"))

        viewModel.updateProfile(updatedUser)
        advanceUntilIdle()

        val state = viewModel.userState.value
        assertTrue("Expected Resource.Error but was $state", state is Resource.Error)
        assertEquals("Errore di rete", (state as Resource.Error).message)
    }

    @Test
    fun `updateProfile should emit Error with default message when exception has no message`() = runTest {
        val updatedUser = testUser.copy(name = "Updated Name")
        coEvery { userRepo.updateUserProfile(any()) } returns Result.failure(Exception())

        viewModel.updateProfile(updatedUser)
        advanceUntilIdle()

        val state = viewModel.userState.value
        assertTrue("Expected Resource.Error but was $state", state is Resource.Error)
        assertEquals("Errore aggiornamento profilo", (state as Resource.Error).message)
    }

    @Test
    fun `loadUser should handle null user from repository`() = runTest {
        coEvery { userRepo.getCurrentUser() } returns null

        viewModel.loadUser()
        advanceUntilIdle()

        val state = viewModel.userState.value
        // Null user results in early return with Resource.Success(null)
        assertTrue("Expected Resource.Success(null) but was $state", state is Resource.Success && state.data == null)
    }

    @Test
    fun `user flow is updated after successful loadUser`() = runTest {
        coEvery { userRepo.getCurrentUser() } returns testUser

        viewModel.loadUser()
        advanceUntilIdle()

        assertEquals(testUser, viewModel.user.value)
    }

    @Test
    fun `user flow reflects updated user after updateProfile success`() = runTest {
        val updatedUser = testUser.copy(bio = "Nuova bio aggiornata")
        coEvery { userRepo.updateUserProfile(any()) } returns Result.success(Unit)

        viewModel.updateProfile(updatedUser)
        advanceUntilIdle()

        assertEquals(updatedUser, viewModel.user.value)
    }
}
