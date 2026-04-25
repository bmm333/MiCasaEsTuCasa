package com.mobile.micasaestucasa.domain.model.chat
/**
 * Rappresents a direct conversation between two users
 *
 * For now i dont think group conversations are needed therefore this is going to be flat.
 * Every Pair hostId renterId is a unique conversation. simplier queries and no dups
 *
 * @property id unique id of the conversation
 * @property hsotId UID of the property owner
 * @property renterID UID of the property renter.
 * @property propertyId id of the property of the conversation.
 * @property lastMessage last message sent in the conversation will serve for the preview.
 * @property LastMessageTimestamp timestamp of the last message sent used to order conversations.
 * @property unreadCount Number of unread messages of the current user
 * */
data class Conversation(
    val id: String,
    val hostId: String,
    val renterId: String,
    val propertyId: String,
    val lastMessage: String = "",
    val lastMessageTimestamp: Long = 0L,
    val unreadCount: Int = 0
)
