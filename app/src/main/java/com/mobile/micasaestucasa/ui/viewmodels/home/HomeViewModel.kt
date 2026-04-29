package com.mobile.micasaestucasa.ui.viewmodels.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.micasaestucasa.domain.model.property.Property
import com.mobile.micasaestucasa.domain.repository.property.PropertyRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val propertyRepo: PropertyRepo
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val categories = listOf(
                Category("Modern", "holiday_village"),
                Category("Rustic", "cabin"),
                Category("Beachfront", "beach_access"),
                Category("Historic", "castle"),
                Category("Urban", "apartment")
            )

            val searchResult = propertyRepo.searchProperties("Roma", "2024-01-01", "2024-12-31", 1)

            searchResult.onSuccess { properties ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    properties = properties,
                    categories = categories
                )
            }.onFailure { exception ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = exception.message ?: "Errore durante il caricamento dei dati"
                )
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        // Logica di filtraggio/ricerca
        if (query.length >= 3) {
            performSearch(query)
        }
    }

    private fun performSearch(query: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = propertyRepo.searchProperties(query, "2024-01-01", "2024-12-31", 1)
            result.onSuccess { properties ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    properties = properties
                )
            }.onFailure { exception ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = exception.message ?: "Errore nella ricerca"
                )
            }
        }
    }
}
