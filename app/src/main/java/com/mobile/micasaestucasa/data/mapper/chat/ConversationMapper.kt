package com.mobile.micasaestucasa.data.mapper.chat

import com.mobile.micasaestucasa.data.dto.chat.ConversationDto
import com.mobile.micasaestucasa.domain.model.chat.Conversation

fun ConversationDto.toDomain(): Conversation = Conversation(
    id = id ?: "",
    hostId = hostId ?: "",
    renterId = renterId ?: "",
    propertyId = propertyId ?: "",
    lastMessage = lastMessage ?: "",
    lastMessageTimestamp = lastMessageTimestamp ?: 0L,
    unreadCount = unreadCount ?: 0
)

fun Conversation.toDto(): ConversationDto = ConversationDto(
    id = id,
    hostId = hostId,
    renterId = renterId,
    propertyId = propertyId,
    lastMessage = lastMessage,
    lastMessageTimestamp = lastMessageTimestamp,
    unreadCount = unreadCount
)
