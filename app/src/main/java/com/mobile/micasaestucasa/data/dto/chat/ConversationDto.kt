package com.mobile.micasaestucasa.data.dto.chat

data class ConversationDto(
    val id: String? = null,
    val hostId: String? = null,
    val renterId: String? = null,
    val propertyId: String? = null,
    val lastMessage: String? = null,
    val lastMessageTimestamp: Long? = null,
    val unreadCount: Int? = null
)
