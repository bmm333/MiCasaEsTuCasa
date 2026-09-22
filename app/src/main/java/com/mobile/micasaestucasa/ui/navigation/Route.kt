package com.mobile.micasaestucasa.ui.navigation

import kotlinx.serialization.Serializable

sealed interface Route {
    @Serializable
    data object Login : Route

    @Serializable
    data object Register : Route

    @Serializable
    data object Home : Route

    @Serializable
    data object Profile : Route

    @Serializable
    data object Wishlist : Route

    @Serializable
    data class PropertyDetail(val propertyId: String) : Route

    @Serializable
    data class BookingRequest(
        val propertyId: String,
        val propertyTitle: String,
        val pricePerDay: Double,
        val hostId: String,
        val availableFrom: String = "",
        val availableTo: String = ""
    ) : Route

    @Serializable
    data object Trips : Route

    @Serializable
    data object HostBookings : Route

    @Serializable
    data object ConversationList : Route

    @Serializable
    data class Chat(
        val conversationId: String,
        val hostId: String,
        val renterId: String,
        val propertyId: String
    ) : Route

    @Serializable
    data object EditProfile : Route

    @Serializable
    data object SignupOnboarding : Route

    @Serializable
    data object PostSignupChoice : Route

    @Serializable
    data class HostIntro(val canSkip: Boolean = true) : Route

    @Serializable
    data object CreateProperty : Route

    @Serializable
    data class EditProperty(val propertyId: String) : Route

    @Serializable
    data object MyProperties : Route

    @Serializable
    data object Admin : Route

    @Serializable
    data object Search : Route

    @Serializable
    data class WriteReview(
        val bookingId: String,
        val propertyId: String,
        val hostId: String,
        val renterId: String
    ) : Route
}
