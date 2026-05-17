package com.mobile.micasaestucasa.ui.viewmodels.admin

import com.mobile.micasaestucasa.domain.model.admin.BookingStats
import com.mobile.micasaestucasa.domain.model.admin.Keyword
import com.mobile.micasaestucasa.domain.model.admin.UserReport

data class AdminUiState(
    val stats: BookingStats? = null,
    val keywords: List<Keyword> = emptyList(),
    val reports: List<UserReport> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
