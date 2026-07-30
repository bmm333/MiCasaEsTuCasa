package com.mobile.micasaestucasa.ui.viewmodels.wishlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.micasaestucasa.domain.repository.user.UserRepo
import com.mobile.micasaestucasa.domain.repository.whishlist.WhishlistRepo
import com.mobile.micasaestucasa.domain.usecase.wishlist.GetWishlistDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WishlistViewModel @Inject constructor(
    private val getWishlistDataUseCase: GetWishlistDataUseCase,
    private val wishlistRepo: WhishlistRepo,
    private val userRepo: UserRepo
) : ViewModel() {

    private val _uiState = MutableStateFlow(WishlistUiState())
    val uiState: StateFlow<WishlistUiState> = _uiState.asStateFlow()

    init {
        loadWishlist()
    }

    /**
     * Toggles a property's saved status with optimistic UI removal.
     * The property is removed from the list immediately for instant UX,
     * then restored if the backend call fails.
     */
    fun toggleSaved(propertyId: String) {
        viewModelScope.launch {
            val user = userRepo.getCurrentUser()
            if (user != null) {
                // Optimistic removal — snapshot current state
                val previousProperties = _uiState.value.properties
                _uiState.update { current ->
                    current.copy(
                        properties = current.properties.filter { it.id != propertyId }
                    )
                }

                wishlistRepo.toggleSavedProperty(user.id, propertyId)
                    .onSuccess {
                        // Reload to ensure consistency
                        loadWishlist()
                    }
                    .onFailure {
                        // Restore on failure
                        _uiState.update { it.copy(properties = previousProperties) }
                    }
            }
        }
    }

    fun loadWishlist() {
        // viewModelScope è fondamentale per chiamare funzioni suspend
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            getWishlistDataUseCase()
                .onSuccess { (props, colls) ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            properties = props,
                            collections = colls,
                            error = null
                        )
                    }
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Errore caricamento"
                        )
                    }
                }
        }
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }
}
