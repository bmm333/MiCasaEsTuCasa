package com.mobile.micasaestucasa.ui.viewmodels.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.micasaestucasa.domain.model.user.User
import com.mobile.micasaestucasa.domain.repository.user.UserRepo
import com.mobile.micasaestucasa.domain.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepo: UserRepo
) : ViewModel() {

    private val _userState = MutableStateFlow<Resource<User?>>(Resource.Loading)
    val userState: StateFlow<Resource<User?>> = _userState.asStateFlow()

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    init {
        loadUser()
    }

    fun loadUser() {
        viewModelScope.launch {
            _userState.value = Resource.Loading
            try {
                val user = userRepo.getCurrentUser()
                _user.value = user
                _userState.value = Resource.Success(user)
            } catch (e: Exception) {
                _userState.value = Resource.Error(e.message ?: "Errore caricamento profilo", e)
            }
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
