package com.mobile.micasaestucasa.ui.viewmodels.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.micasaestucasa.domain.usecase.auth.LoginUseCase
import com.mobile.micasaestucasa.domain.usecase.auth.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(//inject dei usecases
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase): ViewModel()
{
    private val _isLoading= MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    private val _isAuthSuccessful= MutableStateFlow(false)
    val isAuthSuccessful: StateFlow<Boolean> = _isAuthSuccessful.asStateFlow()

    //metodi chaimati dall ui compose
    fun login(email:String,password:String)
    {
        viewModelScope.launch {
            _isLoading.value=true
            _errorMessage.value=null
            //chiamo usecases
            val result=loginUseCase(email,password)
            result.fold(onSuccess = {
                _isAuthSuccessful.value=true
                _isLoading.value=false
            },
                onFailure = {
                    exception ->
                    _errorMessage.value=exception.message?:"Errore di login sconosciuto"
                    _isLoading.value=false
                }
            )
        }
    }
    fun register(email:String,password:String)
    {
        viewModelScope.launch {
            _isLoading.value=true
            _errorMessage.value=null

            val result=registerUseCase(email,password)
            result.fold(
                onSuccess={
                    _isAuthSuccessful.value=true
                    _isLoading.value=false
                },
                onFailure = {
                    exception ->
                    _errorMessage.value=exception.message?:"Errore di registrazione sconosciuto"
                    _isLoading.value=false
                }
            )
        }
    }
    fun clearError()
    {
        _errorMessage.value=null
    }
}