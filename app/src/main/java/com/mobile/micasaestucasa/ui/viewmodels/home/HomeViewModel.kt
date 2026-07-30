package com.mobile.micasaestucasa.ui.viewmodels.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.micasaestucasa.domain.model.property.Property
import com.mobile.micasaestucasa.domain.repository.property.PropertyRepo
import com.mobile.micasaestucasa.domain.repository.whishlist.WhishlistRepo
import com.mobile.micasaestucasa.domain.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = false,
    val properties: List<Property> = emptyList(),
    val categories: List<Category> = emptyList(),
    val error: String? = null,
    val searchQuery: String = ""
)

data class Category(
    val name: String,
    val icon: String
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val propertyRepo: PropertyRepo,
    private val wishlistRepo: WhishlistRepo
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _savedPropertyIds = MutableStateFlow<Set<String>>(emptySet())
    val savedPropertyIds: StateFlow<Set<String>> = _savedPropertyIds.asStateFlow()

    private var authRetried = false

    init {
        loadHomeData()
    }

    fun loadHomeData() {
        viewModelScope.launch(Dispatchers.IO) {
            // Caricamento categorie (asincrono)
            propertyRepo.getCategories().onSuccess { cats ->
                _uiState.update { it.copy(categories = cats) }
            }

            // Sottoscrizione real-time alle proprietà di Firestore
            propertyRepo.getAllPropertiesFlow().collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                    is Resource.Success -> {
                        authRetried = false
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                properties = resource.data,
                                error = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        android.util.Log.e(
                            "HomeVM",
                            "getAllPropertiesFlow error: ${resource.message}",
                            resource.throwable
                        )
                        // Firestore sometimes hasn't synced the auth token yet
                        // right after registration/login — retry ONCE after delay
                        val isPermission =
                            resource.message?.contains("PERMISSION_DENIED", true) == true ||
                                resource.message?.contains("permission", true) == true
                        if (isPermission && !authRetried) {
                            authRetried = true
                            kotlinx.coroutines.delay(2000)
                            loadHomeData()
                            return@collect
                        }
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = resource.message
                            )
                        }
                    }
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        if (query.length >= 3) {
            performSearch(query)
        } else if (query.isEmpty()) {
            loadHomeData()
        }
    }

    private fun performSearch(query: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true) }
            propertyRepo.searchProperties(query, "2024-01-01", "2024-12-31", 1)
                .onSuccess { results ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            properties = results,
                            error = null
                        )
                    }
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Errore nella ricerca"
                        )
                    }
                }
        }
    }

    fun toggleSaved(userId: String, propertyId: String) {
        viewModelScope.launch {
            wishlistRepo.toggleSavedProperty(userId, propertyId)
                .onSuccess { isSaved ->
                    _savedPropertyIds.update { current ->
                        if (isSaved) current + propertyId else current - propertyId
                    }
                }
        }
    }

    fun loadSavedIds(userId: String) {
        if (userId.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            wishlistRepo.getSavedPropertyIds(userId)
                .onSuccess { ids -> _savedPropertyIds.value = ids }
        }
    }
}
