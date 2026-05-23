package com.mobile.micasaestucasa.ui.viewmodels.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.micasaestucasa.domain.model.admin.ReportStatus
import com.mobile.micasaestucasa.domain.model.admin.UserReport
import com.mobile.micasaestucasa.domain.usecase.admin.BanUserUseCase
import com.mobile.micasaestucasa.domain.usecase.admin.GetBookingStatsUseCase
import com.mobile.micasaestucasa.domain.usecase.admin.GetPendingReports
import com.mobile.micasaestucasa.domain.usecase.admin.ManageKeywordsUseCase
import com.mobile.micasaestucasa.domain.usecase.admin.SuspendUserUseCase
import com.mobile.micasaestucasa.domain.repository.admin.AdminRepo
import com.mobile.micasaestucasa.domain.repository.property.PropertyRepo
import com.mobile.micasaestucasa.domain.repository.user.UserRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val manageKeywordsUseCase: ManageKeywordsUseCase,
    private val suspendUserUseCase: SuspendUserUseCase,
    private val banUserUseCase: BanUserUseCase,
    private val getBookingStatsUseCase: GetBookingStatsUseCase,
    private val getPendingReports: GetPendingReports,
    private val adminRepo: AdminRepo,
    private val userRepo: UserRepo,
    private val propertyRepo: PropertyRepo
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    fun loadAll(adminId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // Load stats
            launch {
                getBookingStatsUseCase(adminId)
                    .onSuccess { stats -> _uiState.update { it.copy(stats = stats) } }
                    .onFailure { e ->
                        android.util.Log.e("AdminVM", "Stats failed: ${e.message}", e)
                        _uiState.update { it.copy(error = e.message) }
                    }
            }

            // Load keywords
            launch {
                manageKeywordsUseCase.getAllKeywords()
                    .onSuccess { list -> _uiState.update { it.copy(keywords = list) } }
                    .onFailure { e ->
                        android.util.Log.e("AdminVM", "Keywords failed: ${e.message}", e)
                        _uiState.update { it.copy(error = e.message) }
                    }
            }

            // Load reports
            launch {
                android.util.Log.d("AdminVM", "Loading reports for admin: $adminId")
                getPendingReports(adminId)
                    .onSuccess { list ->
                        android.util.Log.d("AdminVM", "Reports loaded: ${list.size}")
                        _uiState.update { it.copy(reports = list) }
                        resolveUserNames(list)
                        resolvePropertyTitles(list)
                    }
                    .onFailure { e ->
                        android.util.Log.e("AdminVM", "Reports FAILED: ${e.message}", e)
                        _uiState.update { it.copy(error = "Reports: ${e.message}") }
                    }
            }

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun addKeyword(label: String, adminId: String) {
        viewModelScope.launch {
            manageKeywordsUseCase.addKeyword(label, adminId)
                .onSuccess {
                    _uiState.update { it.copy(snackbarMessage = "Keyword added") }
                    manageKeywordsUseCase.getAllKeywords().onSuccess { list ->
                        _uiState.update { it.copy(keywords = list) }
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(snackbarMessage = "Failed: ${e.message}") }
                }
        }
    }

    fun deleteKeyword(keywordId: String, adminId: String) {
        viewModelScope.launch {
            manageKeywordsUseCase.deleteKeyword(keywordId, adminId)
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(
                            keywords = state.keywords.filter { it.id != keywordId },
                            snackbarMessage = "Keyword deleted"
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(snackbarMessage = "Failed: ${e.message}") }
                }
        }
    }

    fun suspendUser(targetUserId: String, adminId: String, reportId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(actionInProgress = it.actionInProgress + reportId) }
            suspendUserUseCase(targetUserId, adminId)
                .onSuccess {
                    // Also resolve the report
                    adminRepo.resolveReport(reportId, ReportStatus.ACCEPTED, adminId)
                    _uiState.update { state ->
                        state.copy(
                            reports = state.reports.filter { it.id != reportId },
                            actionInProgress = state.actionInProgress - reportId,
                            snackbarMessage = "User suspended"
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { state ->
                        state.copy(
                            actionInProgress = state.actionInProgress - reportId,
                            snackbarMessage = "Failed: ${e.message}"
                        )
                    }
                }
        }
    }

    fun banUser(targetUserId: String, adminId: String, reportId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(actionInProgress = it.actionInProgress + reportId) }
            banUserUseCase(targetUserId, adminId)
                .onSuccess {
                    adminRepo.resolveReport(reportId, ReportStatus.ACCEPTED, adminId)
                    _uiState.update { state ->
                        state.copy(
                            reports = state.reports.filter { it.id != reportId },
                            actionInProgress = state.actionInProgress - reportId,
                            snackbarMessage = "User banned permanently"
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { state ->
                        state.copy(
                            actionInProgress = state.actionInProgress - reportId,
                            snackbarMessage = "Failed: ${e.message}"
                        )
                    }
                }
        }
    }

    fun dismissReport(reportId: String, adminId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(actionInProgress = it.actionInProgress + reportId) }
            adminRepo.resolveReport(reportId, ReportStatus.DISMISSED, adminId)
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(
                            reports = state.reports.filter { it.id != reportId },
                            actionInProgress = state.actionInProgress - reportId,
                            snackbarMessage = "Report dismissed"
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { state ->
                        state.copy(
                            actionInProgress = state.actionInProgress - reportId,
                            snackbarMessage = "Failed: ${e.message}"
                        )
                    }
                }
        }
    }

    fun refreshReports(adminId: String) {
        viewModelScope.launch {
            android.util.Log.d("AdminVM", "refreshReports for admin: $adminId")
            getPendingReports(adminId)
                .onSuccess { list ->
                    android.util.Log.d("AdminVM", "refreshReports: ${list.size} reports")
                    _uiState.update { it.copy(reports = list) }
                    resolveUserNames(list)
                    resolvePropertyTitles(list)
                }
                .onFailure { e ->
                    android.util.Log.e("AdminVM", "refreshReports FAILED: ${e.message}", e)
                    _uiState.update { it.copy(error = "Reports: ${e.message}") }
                }
        }
    }

    /** Resolves all user IDs in reports to display names */
    private fun resolveUserNames(reports: List<UserReport>) {
        val allIds = reports.flatMap { listOf(it.reporterId, it.reportedUserId) }.toSet()
        // Only fetch IDs we don't already have
        val known = _uiState.value.userNames
        val missing = allIds.filter { it !in known && it.isNotBlank() }
        if (missing.isEmpty()) return

        viewModelScope.launch {
            val resolved = mutableMapOf<String, String>()
            for (id in missing) {
                try {
                    userRepo.getUserById(id).getOrNull()?.let { user ->
                        resolved[id] = user.name.ifBlank { user.email }
                    } ?: run {
                        resolved[id] = "Unknown user"
                    }
                } catch (_: Exception) {
                    resolved[id] = "Unknown user"
                }
            }
            _uiState.update { it.copy(userNames = it.userNames + resolved) }
        }
    }

    /** Resolves all property IDs in reports to titles */
    private fun resolvePropertyTitles(reports: List<UserReport>) {
        val propIds = reports.mapNotNull { it.propertyId }.filter { it.isNotBlank() }.toSet()
        val known = _uiState.value.propertyTitles
        val missing = propIds.filter { it !in known }
        if (missing.isEmpty()) return

        viewModelScope.launch {
            val resolved = mutableMapOf<String, String>()
            for (id in missing) {
                try {
                    propertyRepo.getPropertyById(id).getOrNull()?.let { prop ->
                        resolved[id] = prop.title
                    } ?: run {
                        resolved[id] = "Deleted property"
                    }
                } catch (_: Exception) {
                    resolved[id] = "Unknown property"
                }
            }
            _uiState.update { it.copy(propertyTitles = it.propertyTitles + resolved) }
        }
    }

    fun consumeSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }
}