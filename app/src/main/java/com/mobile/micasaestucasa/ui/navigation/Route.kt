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
        val hostId: String
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
    data object Admin : Route
}