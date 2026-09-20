package com.mobile.micasaestucasa.ui.viewmodels.booking

import com.mobile.micasaestucasa.domain.model.booking.Booking

data class BookingDisplayInfo(
    val propertyTitle: String,
    val counterpartyName: String,
    val counterpartyId: String
)

/**
 * UI state for the booking screens
 * */
sealed class BookingUiState {
    object Idle : BookingUiState()
    object Loading : BookingUiState()
    data class BookingCreated(val bookingId: String) : BookingUiState()
    data class BookingsLoaded(
        val bookings: List<Booking>,
        val displayInfo: Map<String, BookingDisplayInfo> = emptyMap()
    ) : BookingUiState()
    data class BookingDetail(val booking: Booking) : BookingUiState()
    object ActionSuccess : BookingUiState()
    data class Error(val message: String) : BookingUiState()
}

/**
 * Mocked payment status
 * ofc we cannot integrate a real payment service like in prod
 * */
sealed class PaymentUiStatus {
    object Idle : PaymentUiStatus()
    object Processing : PaymentUiStatus()
    object Success : PaymentUiStatus()
    data class Error(val message: String) : PaymentUiStatus()
}
