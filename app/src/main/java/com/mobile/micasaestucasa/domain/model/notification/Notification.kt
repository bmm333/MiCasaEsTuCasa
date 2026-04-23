package com.mobile.micasaestucasa.domain.model.notification


/**
 * Defines Notification payload
 * @param type: Specifies the type for the routing of the notification
 * @param title: Title of the notification
 * @param body: Content of the notification
 * @param targetId: Id of the contextual entity of this notifiction.
 * */
data class Notification(
    val type:NotificationType,
    val title: String,
    val body: String,
    val targetId: String
);

/**
 * Types of notifications supported by our app
 * each type maps to a different deep link in navhost.
 * */
enum class NotificationType{
    NEW_BOOKING_REQUEST,
    NEW_MESSAGE,
    BOOKING_ACCEPTED,
    BOOKING_REJECTED,
    BOOKING_CANCELLED
}
