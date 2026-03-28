package com.mobile.micasaestucasa.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.micasaestucasa.domain.model.User
import com.mobile.micasaestucasa.domain.usecase.GetCurrentUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class UserViewModel @Inject constructor(private val getCurrentUser: GetCurrentUserUseCase) : ViewModel(){
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user
    fun loadUser()
    {
        viewModelScope.launch{
            _user.value = getCurrentUser()
        }
    }
}