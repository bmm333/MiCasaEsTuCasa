package com.mobile.micasaestucasa.ui.viewmodels.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.micasaestucasa.domain.model.review.Review
import com.mobile.micasaestucasa.domain.model.review.ReviewType
import com.mobile.micasaestucasa.domain.usecase.review.EditReviewUseCase
import com.mobile.micasaestucasa.domain.usecase.review.WriteReviewUseCase
import com.mobile.micasaestucasa.domain.usecase.user.UpdateBadgeUseCase
import com.mobile.micasaestucasa.domain.usecase.user.UpdateRenterScoreUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class ReviewViewModel @Inject constructor(
    private val writeReviewUseCase: WriteReviewUseCase,
    private val editReviewUseCase: EditReviewUseCase,
    private val updateBadgeUseCase: UpdateBadgeUseCase,
    private val updateRenterScoreUseCase: UpdateRenterScoreUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<ReviewUiState>(ReviewUiState.Idle)
    val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()

    /**
     * Writes a review and updates the reputation
     *
     * after wr calls UpdateBadgeUseCase based on review type
     * @param review Review to be written
     * @param userId UID of th author
     * */
    fun writeReview(review: Review, userId:String)
    {
        viewModelScope.launch {
            _uiState.value= ReviewUiState.Loading
            writeReviewUseCase(review,userId)
                .onSuccess {
                    //background task , should not block
                    launch {
                        when (review.reviewType)
                        {
                            ReviewType.PROPERTY_REVIEW -> updateBadgeUseCase(review.targetId)
                            ReviewType.RENTER_REVIEW -> updateRenterScoreUseCase(review.targetId)

                        }
                    }
                    _uiState.value= ReviewUiState.ReviewSubmitted
                }
                .onFailure {
                    _uiState.value= ReviewUiState.Error(
                        it.message?:"Error sending the review."
                    )
                }
        }
        /**
         * Modifies an exisint review
         * @param review Review to be modified
         * @param userId UID of the author
         * */
        fun editReview(review: Review, userId: String) {
            viewModelScope.launch {
                _uiState.value = ReviewUiState.Loading
                editReviewUseCase(review, userId)
                    .onSuccess { _uiState.value = ReviewUiState.ReviewSubmitted }
                    .onFailure {
                        _uiState.value = ReviewUiState.Error(
                            it.message ?: "Errore nella modifica della recensione"
                        )
                    }
            }
        }

        fun resetState() { _uiState.value = ReviewUiState.Idle }

    }

}