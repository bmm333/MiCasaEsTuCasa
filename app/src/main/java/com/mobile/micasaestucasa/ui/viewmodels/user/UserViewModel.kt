package com.mobile.micasaestucasa.ui.viewmodels.user

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.micasaestucasa.domain.model.booking.BookingStatus
import com.mobile.micasaestucasa.domain.model.user.User
import com.mobile.micasaestucasa.domain.model.user.UserRole
import com.mobile.micasaestucasa.domain.repository.booking.BookingRepo
import com.mobile.micasaestucasa.domain.repository.property.PropertyRepo
import com.mobile.micasaestucasa.domain.repository.user.UserRepo
import com.mobile.micasaestucasa.domain.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HostStats(
    val propertyCount: Int = 0,
    val bookingCount: Int = 0,
    val totalRevenue: Double = 0.0,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepo: UserRepo,
    private val propertyRepo: PropertyRepo,
    private val bookingRepo: BookingRepo,
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    private val IS_HOST_KEY = booleanPreferencesKey("is_host")

    private val _userState = MutableStateFlow<Resource<User?>>(Resource.Loading)
    val userState: StateFlow<Resource<User?>> = _userState.asStateFlow()

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _hostStats = MutableStateFlow(HostStats())
    val hostStats: StateFlow<HostStats> = _hostStats.asStateFlow()

    val isHost: StateFlow<Boolean> = dataStore.data
        .map { prefs -> prefs[IS_HOST_KEY] ?: false }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = false
        )

    init {
        loadUser()
    }

    private suspend fun persistIsHost(value: Boolean) {
        dataStore.edit { prefs -> prefs[IS_HOST_KEY] = value }
    }

    fun loadUser() {
        viewModelScope.launch {
            _userState.value = Resource.Loading
            try {
                val user = userRepo.getCurrentUser()
                if (user == null) {
                    _userState.value = Resource.Success(null)
                    return@launch
                }
                _user.value = user
                _userState.value = Resource.Success(user)

                val hasOwnerRole = user.roles.contains(UserRole.OWNER)

                if (hasOwnerRole) {
                    persistIsHost(true)
                    loadHostStats(user.id)
                } else {
                    val propertiesResult = propertyRepo.getPropertiesByOwner(user.id)
                    val hasProperties = propertiesResult.getOrDefault(emptyList()).isNotEmpty()

                    if (hasProperties) {
                        val newRoles = user.roles + UserRole.OWNER
                        userRepo.updateUserRolesAndBadge(user.id, newRoles, com.mobile.micasaestucasa.domain.model.user.UserBadge.NEW_HOST)
                        persistIsHost(true)
                        val updatedUser = user.copy(roles = newRoles, badge = com.mobile.micasaestucasa.domain.model.user.UserBadge.NEW_HOST)
                        _user.value = updatedUser
                        _userState.value = Resource.Success(updatedUser)
                        loadHostStats(user.id)
                    } else {
                        persistIsHost(false)
                    }
                }
            } catch (e: Exception) {
                _userState.value = Resource.Error(e.message ?: "Errore caricamento profilo", e)
            }
        }
    }

    private fun loadHostStats(userId: String) {
        viewModelScope.launch {
            _hostStats.value = _hostStats.value.copy(isLoading = true, error = null)
            try {
                val propertiesDeferred = async { propertyRepo.getPropertiesByOwner(userId) }
                val bookingsDeferred = async { bookingRepo.getBookingsForHost(userId) }

                val propertiesResult = propertiesDeferred.await()
                val bookingsResult = bookingsDeferred.await()

                if (propertiesResult.isSuccess && bookingsResult.isSuccess) {
                    val properties = propertiesResult.getOrDefault(emptyList())
                    val bookings = bookingsResult.getOrDefault(emptyList())

                    val revenue = bookings
                        .filter { it.status == BookingStatus.ACCEPTED || it.status == BookingStatus.COMPLETED }
                        .sumOf { it.totalPrice }

                    _hostStats.value = HostStats(
                        propertyCount = properties.size,
                        bookingCount = bookings.size,
                        totalRevenue = revenue,
                        isLoading = false
                    )
                } else {
                    _hostStats.value = _hostStats.value.copy(
                        isLoading = false,
                        error = "Impossibile caricare le statistiche"
                    )
                }
            } catch (e: Exception) {
                _hostStats.value = _hostStats.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun demoteFromHost(userId: String) {
        viewModelScope.launch {
            persistIsHost(false)
            val user = _user.value ?: return@launch
            val newRoles = user.roles.filter { it != UserRole.OWNER }
            userRepo.updateUserRolesAndBadge(userId, newRoles, com.mobile.micasaestucasa.domain.model.user.UserBadge.NEW_RENTER)
            val updatedUser = user.copy(roles = newRoles, badge = com.mobile.micasaestucasa.domain.model.user.UserBadge.NEW_RENTER)
            _user.value = updatedUser
            _userState.value = Resource.Success(updatedUser)
        }
    }

    fun updateProfile(updatedUser: User) {
        viewModelScope.launch {
            _userState.value = Resource.Loading
            val result = userRepo.updateUserProfile(updatedUser)
            result.onSuccess {
                _user.value = updatedUser
                _userState.value = Resource.Success(updatedUser)
            }.onFailure { e ->
                _userState.value = Resource.Error(e.message ?: "Errore aggiornamento profilo", e)
            }
        }
    }
}
