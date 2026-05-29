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

    /**
     * DEV ONLY — seeds Firestore with sample properties so the HomeScreen
     * has something to show. Call once then remove or guard behind BuildConfig.DEBUG.
     */
    fun seedSampleProperties() {
        val samples = listOf(
            Property(
                id = "",
                ownerId = "dev_seed",
                title = "Villa sul Mare",
                description = "Splendida villa con vista panoramica sul mare. " +
                    "Piscina privata, giardino tropicale e accesso diretto alla spiaggia.",
                latitude = 40.6501,
                longitude = 14.6009,
                city = "Amalfi",
                pricePerDay = 280.0,
                capacity = 6,
                keywords = listOf("piscina", "vista mare", "wifi", "parcheggio"),
                imageUrls = listOf(
                    "https://images.unsplash.com/photo-1600585154340-be6161a56a0c?w=800"
                ),
                availableFrom = "2026-06-01",
                availableTo = "2026-09-30",
                rating = 4.8,
                reviewsCount = 24
            ),
            Property(
                id = "",
                ownerId = "dev_seed",
                title = "Loft nel Centro Storico",
                description = "Loft moderno nel cuore di Roma, a due passi dal Colosseo. " +
                    "Design contemporaneo con travi a vista e terrazza privata.",
                latitude = 41.8902,
                longitude = 12.4922,
                city = "Roma",
                pricePerDay = 150.0,
                capacity = 4,
                keywords = listOf("centro", "wifi", "terrazza", "aria condizionata"),
                imageUrls = listOf(
                    "https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?w=800"
                ),
                availableFrom = "2026-05-01",
                availableTo = "2026-12-31",
                rating = 4.6,
                reviewsCount = 42
            ),
            Property(
                id = "",
                ownerId = "dev_seed",
                title = "Chalet di Montagna",
                description = "Chalet accogliente con camino, sauna e vista sulle Dolomiti. " +
                    "Perfetto per una vacanza rilassante in montagna.",
                latitude = 46.4102,
                longitude = 11.8440,
                city = "Cortina",
                pricePerDay = 320.0,
                capacity = 8,
                keywords = listOf("montagna", "camino", "sauna", "parcheggio", "wifi"),
                imageUrls = listOf(
                    "https://images.unsplash.com/photo-1518780664697-55e3ad937233?w=800"
                ),
                availableFrom = "2026-06-15",
                availableTo = "2026-03-31",
                rating = 4.9,
                reviewsCount = 18
            ),
            Property(
                id = "",
                ownerId = "dev_seed",
                title = "Appartamento Vista Duomo",
                description = "Elegante appartamento con vista diretta sul Duomo di Firenze. " +
                    "Arredato con gusto, cucina attrezzata e due camere da letto.",
                latitude = 43.7731,
                longitude = 11.2560,
                city = "Firenze",
                pricePerDay = 190.0,
                capacity = 4,
                keywords = listOf("centro", "vista", "wifi", "cucina"),
                imageUrls = listOf(
                    "https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?w=800"
                ),
                availableFrom = "2026-05-01",
                availableTo = "2026-11-30",
                rating = 4.7,
                reviewsCount = 36
            ),
            Property(
                id = "",
                ownerId = "dev_seed",
                title = "Trullo Tradizionale",
                description = "Autentico trullo pugliese ristrutturato con piscina. " +
                    "Immerso tra gli ulivi, esperienza unica nel sud Italia.",
                latitude = 40.7834,
                longitude = 17.2372,
                city = "Alberobello",
                pricePerDay = 130.0,
                capacity = 3,
                keywords = listOf("piscina", "tradizionale", "wifi", "giardino"),
                imageUrls = listOf(
                    "https://images.unsplash.com/photo-1564013799919-ab600027ffc6?w=800"
                ),
                availableFrom = "2026-04-01",
                availableTo = "2026-10-31",
                rating = 4.95,
                reviewsCount = 53
            ),
            Property(
                id = "",
                ownerId = "dev_seed",
                title = "Casa sul Lago",
                description = "Romantica casa sul Lago di Como con molo privato. " +
                    "Giardino fiorito, barbecue e kayak inclusi.",
                latitude = 45.9870,
                longitude = 9.2572,
                city = "Como",
                pricePerDay = 250.0,
                capacity = 5,
                keywords = listOf("lago", "molo", "wifi", "barbecue", "kayak"),
                imageUrls = listOf(
                    "https://images.unsplash.com/photo-1580587771525-78b9dba3b914?w=800"
                ),
                availableFrom = "2026-05-15",
                availableTo = "2026-09-15",
                rating = 4.85,
                reviewsCount = 29
            )
        )

        viewModelScope.launch(Dispatchers.IO) {
            samples.forEach { property ->
                propertyRepo.createProperty(property)
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
