package com.mobile.micasaestucasa.ui.viewmodels.admin

import com.mobile.micasaestucasa.domain.model.admin.ActionedUser
import com.mobile.micasaestucasa.domain.model.admin.BookingStats
import com.mobile.micasaestucasa.domain.model.admin.Keyword
import com.mobile.micasaestucasa.domain.model.admin.UserReport

data class AdminUiState(
    val stats: BookingStats? = null,
    val keywords: List<Keyword> = emptyList(),
    val reports: List<UserReport> = emptyList(),
    /** Maps userId -> display name for report cards */
    val userNames: Map<String, String> = emptyMap(),
    /** Maps propertyId -> title for report cards */
    val propertyTitles: Map<String, String> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val snackbarMessage: String? = null,
    /** Tracks which report action is in progress (reportId -> true) */
    val actionInProgress: Set<String> = emptySet(),
    /** Suspended/banned users shown in the User Management tab */
    val actionedUsers: List<ActionedUser> = emptyList(),
    /** Tracks which user reactivation is in progress */
    val reactivateInProgress: Set<String> = emptySet()
)
