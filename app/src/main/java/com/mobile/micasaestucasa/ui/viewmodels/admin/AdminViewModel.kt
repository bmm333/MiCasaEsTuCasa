package com.mobile.micasaestucasa.ui.viewmodels.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.micasaestucasa.domain.usecase.admin.BanUserUseCase
import com.mobile.micasaestucasa.domain.usecase.admin.GetBookingStatsUseCase
import com.mobile.micasaestucasa.domain.usecase.admin.GetPendingReports
import com.mobile.micasaestucasa.domain.usecase.admin.ManageKeywordsUseCase
import com.mobile.micasaestucasa.domain.usecase.admin.SuspendUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class AdminViewModel @Inject constructor(
    private val manageKeywordsUseCase: ManageKeywordsUseCase,
    private val suspendUserUseCase: SuspendUserUseCase,
    private val banUserUseCase: BanUserUseCase,
    private val getBookingStatsUseCase: GetBookingStatsUseCase,
    private val getPendingReports: GetPendingReports
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    fun loadAll(adminId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            launch {
                getBookingStatsUseCase(adminId).onSuccess {
                    _uiState.value = _uiState.value.copy(stats = it)
                }
            }
            launch {
                manageKeywordsUseCase.getAllKeywords().onSuccess {
                    _uiState.value = _uiState.value.copy(keywords = it)
                }
            }
            launch {
                getPendingReports(adminId).onSuccess {
                    _uiState.value = _uiState.value.copy(reports = it)
                }
            }

            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun addKeyword(label: String, adminId: String) {
        viewModelScope.launch {
            manageKeywordsUseCase.addKeyword(label, adminId).onSuccess {
                manageKeywordsUseCase.getAllKeywords().onSuccess { list ->
                    _uiState.value = _uiState.value.copy(keywords = list)
                }
            }
        }
    }

    fun deleteKeyword(keywordId: String, adminId: String) {
        viewModelScope.launch {
            manageKeywordsUseCase.deleteKeyword(keywordId, adminId).onSuccess {
                _uiState.value = _uiState.value.copy(
                    keywords = _uiState.value.keywords.filter { it.id != keywordId }
                )
            }
        }
    }

    fun suspendUser(targetUserId: String, adminId: String) {
        viewModelScope.launch {
            suspendUserUseCase(targetUserId, adminId).onSuccess {
                _uiState.value = _uiState.value.copy(
                    reports = _uiState.value.reports.filter {
                        it.reportedUserId != targetUserId
                    }
                )
            }
        }
    }

    fun banUser(targetUserId: String, adminId: String) {
        viewModelScope.launch {
            banUserUseCase(targetUserId, adminId).onSuccess {
                _uiState.value = _uiState.value.copy(
                    reports = _uiState.value.reports.filter {
                        it.reportedUserId != targetUserId
                    }
                )
            }
        }
    }
}