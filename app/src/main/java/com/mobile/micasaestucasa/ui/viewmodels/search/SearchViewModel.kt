package com.mobile.micasaestucasa.ui.viewmodels.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.micasaestucasa.domain.model.search.SearchQuery
import com.mobile.micasaestucasa.domain.model.search.SearchSortOrder
import com.mobile.micasaestucasa.domain.usecase.search.SearchAvaliblePropertiesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(private val searchUseCase: SearchAvaliblePropertiesUseCase) : ViewModel() {
    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    // needed for the maps ui
    private val _selectedPropertyId = MutableStateFlow<String?>(null)
    val selectedPropertyId: StateFlow<String?> = _selectedPropertyId.asStateFlow()

    // current filters
    private var currentQuery: SearchQuery? = null
    private var currentCategories: List<String> = emptyList()

    /**
     * Executes a query with given parameters
     * Updates currentQuery for the next modification of filters
     *
     * @param city City of dest
     * @param startDate Start date of the stay
     * @param endDate End date of the stay
     * @param guestsCount Number of guests
     * @param keywords optional keywords
     * */
    fun search(
        city: String,
        startDate: String,
        endDate: String,
        guestsCount: Int,
        keywords: List<String> = emptyList(),
        maxPricePerDay: Double? = null,
        sortOrder: SearchSortOrder = SearchSortOrder.RELEVANCE
    ) {
        val normalizedCity = city.trim().replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(java.util.Locale.ITALY) else it.toString()
        }
        val query = SearchQuery(
            city = normalizedCity,
            startDate = startDate,
            endDate = endDate,
            guestsCount = guestsCount,
            keywords = keywords,
            maxPricePerDay = maxPricePerDay,
            sortOrder = sortOrder
        )
        executeSearch(query)
    }

    /**
     * Simply applies new order to precedent results without recaling firestore
     * */
    fun applySortOrder(sortOrder: SearchSortOrder) {
        val query = currentQuery ?: return
        executeSearch(query.copy(sortOrder = sortOrder))
    }
    fun applyMaxPrice(maxPrice: Double?) {
        val query = currentQuery ?: return
        executeSearch(query.copy(maxPricePerDay = maxPrice))
    }

    /**
     * Applies category filters by merging them into the keywords list.
     * Categories such as "piscina", "montagna", "wifi" map directly to keywords
     * already supported by the search backend.
     * @param categories list of category keywords to apply (empty = clear category filter)
     */
    fun applyCategories(categories: List<String>) {
        val query = currentQuery ?: return
        // Keep non-category keywords and add the new categories
        val base = query.keywords.filterNot { it in (currentCategories) }
        currentCategories = categories
        executeSearch(query.copy(keywords = base + categories))
    }

    // choose in map
    fun selectProperty(propertyId: String?) {
        _selectedPropertyId.value = propertyId
    }
    fun reset() {
        currentQuery = null
        currentCategories = emptyList()
        _selectedPropertyId.value = null
        _uiState.value = SearchUiState.Idle
    }
    private fun executeSearch(query: SearchQuery) {
        currentQuery = query
        viewModelScope.launch {
            _uiState.value = SearchUiState.Loading
            searchUseCase(query)
                .onSuccess { results ->
                    _uiState.value = if (results.isEmpty()) {
                        SearchUiState.Empty
                    } else {
                        SearchUiState.Results(results, query)
                    }
                }
                .onFailure {
                    _uiState.value = SearchUiState.Error(it.message ?: "Errore nella ricerca")
                }
        }
    }
}
