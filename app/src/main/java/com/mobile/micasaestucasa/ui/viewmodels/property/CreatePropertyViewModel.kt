package com.mobile.micasaestucasa.ui.viewmodels.property

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.micasaestucasa.domain.model.property.Property
import com.mobile.micasaestucasa.domain.model.user.UserBadge
import com.mobile.micasaestucasa.domain.model.user.UserRole
import com.mobile.micasaestucasa.domain.repository.admin.AdminRepo
import com.mobile.micasaestucasa.domain.repository.user.UserRepo
import com.mobile.micasaestucasa.domain.usecase.property.CreatePropertyUseCase
import com.mobile.micasaestucasa.domain.usecase.property.GetPropertyByIdUseCase
import com.mobile.micasaestucasa.domain.usecase.property.UpdatePropertyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PropertyDraft(
    val propertyType: String = "",
    val city: String = "",
    val address: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val capacity: Int = 1,
    val keywords: List<String> = emptyList(),
    val imageUris: List<String> = emptyList(),
    val title: String = "",
    val description: String = "",
    val pricePerDay: Double = 0.0,
    val availableFrom: String = "",
    val availableTo: String = ""
)

sealed class CreatePropertyState {
    object Idle : CreatePropertyState()
    object Submitting : CreatePropertyState()
    data class Success(val propertyId: String) : CreatePropertyState()
    data class Error(val message: String) : CreatePropertyState()
}

private val KNOWN_PROPERTY_TYPES = listOf(
    "Appartamento", "Casa", "Villa", "Chalet", "Hotel", "Spiaggia"
)

@HiltViewModel
class CreatePropertyViewModel @Inject constructor(
    private val createPropertyUseCase: CreatePropertyUseCase,
    private val updatePropertyUseCase: UpdatePropertyUseCase,
    private val getPropertyByIdUseCase: GetPropertyByIdUseCase,
    private val adminRepo: AdminRepo,
    private val userRepo: UserRepo
) : ViewModel() {

    private val _currentStep = MutableStateFlow(0)
    val currentStep: StateFlow<Int> = _currentStep.asStateFlow()

    private val _draft = MutableStateFlow(PropertyDraft())
    val draft: StateFlow<PropertyDraft> = _draft.asStateFlow()

    private val _submitState = MutableStateFlow<CreatePropertyState>(CreatePropertyState.Idle)
    val submitState: StateFlow<CreatePropertyState> = _submitState.asStateFlow()

    private var editingPropertyId: String? = null
    val isEditMode: Boolean get() = editingPropertyId != null

    /** Keywords loaded from Firestore (admin-managed) */
    private val _availableKeywords = MutableStateFlow<List<String>>(emptyList())
    val availableKeywords: StateFlow<List<String>> = _availableKeywords.asStateFlow()

    val totalSteps = 8

    init {
        loadKeywords()
    }

    private fun loadKeywords() {
        viewModelScope.launch {
            adminRepo.getAllKeywords().onSuccess { list ->
                _availableKeywords.value = list.map { it.label }
            }
        }
    }

    fun nextStep() { if (_currentStep.value < totalSteps - 1) _currentStep.value++ }
    fun prevStep() { if (_currentStep.value > 0) _currentStep.value-- }

    fun updatePropertyType(type: String) { _draft.value = _draft.value.copy(propertyType = type) }
    fun updateCity(city: String) { _draft.value = _draft.value.copy(city = city) }
    fun updateAddress(address: String) { _draft.value = _draft.value.copy(address = address) }
    fun updateLocation(lat: Double, lng: Double) { _draft.value = _draft.value.copy(latitude = lat, longitude = lng) }
    fun updateCapacity(capacity: Int) { _draft.value = _draft.value.copy(capacity = capacity.coerceAtLeast(1)) }

    fun toggleKeyword(keyword: String) {
        val current = _draft.value.keywords.toMutableList()
        if (current.contains(keyword)) current.remove(keyword) else current.add(keyword)
        _draft.value = _draft.value.copy(keywords = current)
    }

    fun addImageUri(uri: String) {
        val current = _draft.value.imageUris.toMutableList()
        if (current.size < 5 && !current.contains(uri)) current.add(uri)
        _draft.value = _draft.value.copy(imageUris = current)
    }

    fun removeImageUri(uri: String) { _draft.value = _draft.value.copy(imageUris = _draft.value.imageUris - uri) }
    fun updateTitle(title: String) { _draft.value = _draft.value.copy(title = title) }
    fun updateDescription(description: String) { _draft.value = _draft.value.copy(description = description) }
    fun updatePrice(price: Double) { _draft.value = _draft.value.copy(pricePerDay = price) }
    fun updateAvailability(from: String, to: String) { _draft.value = _draft.value.copy(availableFrom = from, availableTo = to) }

    fun loadForEdit(propertyId: String) {
        viewModelScope.launch {
            _submitState.value = CreatePropertyState.Submitting
            getPropertyByIdUseCase(propertyId)
                .onSuccess { property ->
                    editingPropertyId = property.id
                    val typeKeyword = property.keywords.firstOrNull { it in KNOWN_PROPERTY_TYPES } ?: ""
                    val amenityKeywords = property.keywords.filter { it != typeKeyword }
                    _draft.value = PropertyDraft(
                        propertyType = typeKeyword,
                        city = property.city,
                        address = "",
                        latitude = property.latitude,
                        longitude = property.longitude,
                        capacity = property.capacity,
                        keywords = amenityKeywords,
                        imageUris = property.imageUrls,
                        title = property.title,
                        description = property.description,
                        pricePerDay = property.pricePerDay,
                        availableFrom = property.availableFrom,
                        availableTo = property.availableTo
                    )
                    _currentStep.value = 0
                    _submitState.value = CreatePropertyState.Idle
                }
                .onFailure { e ->
                    _submitState.value = CreatePropertyState.Error(e.message ?: "Errore caricamento proprietà")
                }
        }
    }

    fun publish(ownerId: String) {
        val d = _draft.value
        viewModelScope.launch {
            _submitState.value = CreatePropertyState.Submitting
            val property = Property(
                id = "",
                ownerId = ownerId,
                title = d.title.trim(),
                description = d.description.trim(),
                latitude = d.latitude,
                longitude = d.longitude,
                city = d.city.trim().replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(java.util.Locale.ITALY) else it.toString()
                },
                pricePerDay = d.pricePerDay,
                capacity = d.capacity,
                keywords = buildList {
                    if (d.propertyType.isNotBlank()) add(d.propertyType)
                    addAll(d.keywords)
                },
                imageUrls = d.imageUris,
                availableFrom = d.availableFrom,
                availableTo = d.availableTo
            )
            createPropertyUseCase(property)
                .onSuccess { id ->
                    // Promote user to OWNER role
                    try {
                        val currentUser = userRepo.getCurrentUser()
                        if (currentUser != null && !currentUser.roles.contains(UserRole.OWNER)) {
                            val updated = currentUser.copy(
                                roles = currentUser.roles + UserRole.OWNER,
                                badge = UserBadge.NEW_HOST
                            )
                            userRepo.updateUserProfile(updated)
                        }
                    } catch (_: Exception) { /* role promotion best-effort */ }
                    _submitState.value = CreatePropertyState.Success(id)
                }
                .onFailure { e ->
                    _submitState.value = CreatePropertyState.Error(e.message ?: "Errore nella pubblicazione")
                }
        }
    }

    fun saveEdit(ownerId: String) {
        val propertyId = editingPropertyId ?: return
        val d = _draft.value
        viewModelScope.launch {
            _submitState.value = CreatePropertyState.Submitting
            val property = Property(
                id = propertyId,
                ownerId = ownerId,
                title = d.title.trim(),
                description = d.description.trim(),
                latitude = d.latitude,
                longitude = d.longitude,
                city = d.city.trim().replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(java.util.Locale.ITALY) else it.toString()
                },
                pricePerDay = d.pricePerDay,
                capacity = d.capacity,
                keywords = buildList {
                    if (d.propertyType.isNotBlank()) add(d.propertyType)
                    addAll(d.keywords)
                },
                imageUrls = d.imageUris,
                availableFrom = d.availableFrom,
                availableTo = d.availableTo
            )
            updatePropertyUseCase(property)
                .onSuccess { id -> _submitState.value = CreatePropertyState.Success(id) }
                .onFailure { e ->
                    _submitState.value = CreatePropertyState.Error(e.message ?: "Errore aggiornamento")
                }
        }
    }

    fun resetSubmitState() { _submitState.value = CreatePropertyState.Idle }

    fun reset() {
        editingPropertyId = null
        _currentStep.value = 0
        _draft.value = PropertyDraft()
        _submitState.value = CreatePropertyState.Idle
    }
}
