package com.mobile.micasaestucasa.ui.viewmodels.property

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.micasaestucasa.domain.model.property.Property
import com.mobile.micasaestucasa.domain.usecase.property.CreatePropertyUseCase
import com.mobile.micasaestucasa.domain.usecase.property.GetOwnerPropertiesUseCase
import com.mobile.micasaestucasa.domain.usecase.property.SearchPropertiesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PropertyViewModel @Inject constructor(private val searchPropertiesUseCase: SearchPropertiesUseCase,
    private val getOwnerPropertiesUseCase: GetOwnerPropertiesUseCase,
    private val createPropertyUseCase: CreatePropertyUseCase): ViewModel()
{
    private val _uiState = MutableStateFlow<PropertyUiState>(PropertyUiState.Idle)
    val uiState: StateFlow<PropertyUiState> = _uiState.asStateFlow()
    //cache last search - lower the firestore rate if city and params dont change
    private var lastSearchParams: SearchParams? = null

    /**
     * Searches properties with given params
     * if the params are identical to last search and the state is  already
     * in SearchSuccess we do not make another request
     * @param city Destination city
     * @param startDate Begining date
     * @param endDate End date of the stay
     * @param capacity Minum number of the guests
     * @param  keywords List of keywords to search
     * */
    fun searchProperties(
        city: String,
        startDate: String,
        endDate: String,
        capacity: Int,
        keywords: List<String> = emptyList()
    ) {
        val params = SearchParams(city, startDate, endDate, capacity, keywords)
        if (params == lastSearchParams && _uiState.value is PropertyUiState.SearchSuccess) {
            return
        }
        viewModelScope.launch {
            _uiState.value = PropertyUiState.Loading
            searchPropertiesUseCase(city, startDate, endDate, capacity, keywords)
                .onSuccess { properties ->
                    lastSearchParams = params // FIXATA LA VIRGOLA
                    _uiState.value = PropertyUiState.SearchSuccess(properties)
                }
                .onFailure { error ->
                    _uiState.value = PropertyUiState.Error(error.message ?: "Error during search")
                }
        }
    }
    /**
     * Loads all the properties of the logged
     * owner
     * @param ownerId Firebase UID of the owner
     * */
    fun loadOwnerProperties(ownerId:String)
    {
        viewModelScope.launch {
            _uiState.value= PropertyUiState.Loading
            getOwnerPropertiesUseCase(ownerId)
                .onSuccess { _uiState.value= PropertyUiState.OwnerSuccess(it) }
                .onFailure {
                    _uiState.value= PropertyUiState.Error(it.message?:"Error on loading properties")
                }
        }
    }
    /**
     * Creates a new property
     * After the creation it reloads the list of the properties of the owner
     * to keep the ui updated
     *
     * @param property Property to be created
     * @param ownerId Firebase UID of the owner
     * */
    fun createProperty(property: Property, ownerId:String)
    {
        viewModelScope.launch {
            _uiState.value = PropertyUiState.Loading
            createPropertyUseCase(property)
                .onSuccess { loadOwnerProperties(ownerId) }
                .onFailure {
                    _uiState.value = PropertyUiState.Error(
                        it.message ?: "Errore creating the property"
                    )
                }
        }
    }
    fun resetState() {
        _uiState.value = PropertyUiState.Idle
    }
    private data class SearchParams(
        val city: String,
        val startDate: String,
        val endDate: String,
        val capacity: Int,
        val keywords: List<String>
    )
}






