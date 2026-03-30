package com.mobile.micasaestucasa.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.mobile.micasaestucasa.ui.navigation.Route
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val auth: FirebaseAuth // Injected via Hilt
) : ViewModel() {
    val startDestination: Route = if (auth.currentUser != null) Route.Home else Route.Login
}