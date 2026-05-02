package com.mobile.micasaestucasa.ui.viewmodels.search

import com.mobile.micasaestucasa.domain.model.search.SearchQuery
import com.mobile.micasaestucasa.domain.model.search.SearchResult

sealed class SearchUiState {
    object Idle : SearchUiState()
    object Loading : SearchUiState()
    data class Results(val results: List<SearchResult>, val query: SearchQuery) : SearchUiState()
    object Empty : SearchUiState()
    data class Error(val message: String) : SearchUiState()
}
