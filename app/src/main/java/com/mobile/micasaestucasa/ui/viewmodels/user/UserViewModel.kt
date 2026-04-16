package com.mobile.micasaestucasa.ui.viewmodels.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.micasaestucasa.domain.model.user.User
import com.mobile.micasaestucasa.domain.usecase.user.GetCurrentUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(private val getCurrentUser: GetCurrentUserUseCase) : ViewModel() {
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user
    fun loadUser() {
        viewModelScope.launch {
            _user.value = getCurrentUser()
        }
    }
}
