package com.mobile.micasaestucasa.ui.viewmodels.review

import com.mobile.micasaestucasa.domain.model.review.Review

sealed class ReviewUiState {
    object Idle : ReviewUiState()
    object Loading : ReviewUiState()
    object ReviewSubmitted : ReviewUiState()
    data class ReviewsLoaded(val reviews: List<Review>) : ReviewUiState()
    data class Error(val message: String) : ReviewUiState()
}
