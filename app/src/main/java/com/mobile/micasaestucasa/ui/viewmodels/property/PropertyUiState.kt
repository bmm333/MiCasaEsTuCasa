package com.mobile.micasaestucasa.ui.viewmodels.property

import com.mobile.micasaestucasa.domain.model.property.Property

/**
 * UI state for the related property screens
 * sealed class eliminating impossible states such as loading + error together
 * */
sealed class PropertyUiState {
    object Idle: PropertyUiState()
    object Loading: PropertyUiState()
    data class SearchSuccess(val properties: List<Property>): PropertyUiState()
    data class DetailSuccess(val property: Property): PropertyUiState()
    data class OwnerSuccess(val properties: List<Property>): PropertyUiState()
    data class Error(val message: String): PropertyUiState()
}